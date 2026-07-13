<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\WalletInvitation;
use App\Notifications\WalletInvitationNotification;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

class WalletSharingTest extends TestCase
{
    use RefreshDatabase;

    private User $owner;
    private User $member;

    protected function setUp(): void
    {
        parent::setUp();
        $this->owner  = User::factory()->create();
        $this->member = User::factory()->create();
    }

    private function ownerWallet()
    {
        return $this->owner->wallets()->where('type', 'cash')->first();
    }

    private function invite(): WalletInvitation
    {
        return $this->ownerWallet()->invitations()->create([
            'inviter_id' => $this->owner->id,
            'email'      => $this->member->email,
            'token'      => str_repeat('a', 64),
            'status'     => 'pending',
            'expires_at' => now()->addDays(7),
        ]);
    }

    // --- Invite ---

    public function test_owner_can_invite_by_email(): void
    {
        Notification::fake();
        $wallet = $this->ownerWallet();

        $this->actingAs($this->owner)->postJson("/api/wallets/{$wallet->id}/invitations", [
            'email' => $this->member->email,
        ])->assertStatus(201);

        $this->assertDatabaseHas('wallet_invitations', [
            'wallet_id' => $wallet->id,
            'email'     => $this->member->email,
            'status'    => 'pending',
        ]);

        Notification::assertSentOnDemand(WalletInvitationNotification::class);
    }

    public function test_cannot_invite_self(): void
    {
        $wallet = $this->ownerWallet();

        $this->actingAs($this->owner)->postJson("/api/wallets/{$wallet->id}/invitations", [
            'email' => $this->owner->email,
        ])->assertStatus(422);
    }

    public function test_non_owner_cannot_invite(): void
    {
        $wallet = $this->ownerWallet();

        $this->actingAs($this->member)->postJson("/api/wallets/{$wallet->id}/invitations", [
            'email' => 'someone@example.com',
        ])->assertStatus(403);
    }

    public function test_cannot_send_duplicate_pending_invite(): void
    {
        $this->invite();
        $wallet = $this->ownerWallet();

        $this->actingAs($this->owner)->postJson("/api/wallets/{$wallet->id}/invitations", [
            'email' => $this->member->email,
        ])->assertStatus(422);
    }

    // --- List pending ---

    public function test_member_sees_pending_invitations(): void
    {
        $this->invite();

        $response = $this->actingAs($this->member)->getJson('/api/invitations')->assertOk();
        $this->assertCount(1, $response->json('data'));
    }

    // --- Accept / Decline ---

    public function test_member_can_accept_invitation(): void
    {
        $invitation = $this->invite();

        $this->actingAs($this->member)
             ->postJson("/api/invitations/{$invitation->token}/accept")
             ->assertOk();

        $this->assertDatabaseHas('wallet_invitations', [
            'id'     => $invitation->id,
            'status' => 'accepted',
        ]);
        $this->assertDatabaseHas('wallet_user', [
            'wallet_id' => $this->ownerWallet()->id,
            'user_id'   => $this->member->id,
            'role'      => 'member',
        ]);
    }

    public function test_member_can_decline_invitation(): void
    {
        $invitation = $this->invite();

        $this->actingAs($this->member)
             ->postJson("/api/invitations/{$invitation->token}/decline")
             ->assertOk();

        $this->assertDatabaseHas('wallet_invitations', [
            'id'     => $invitation->id,
            'status' => 'declined',
        ]);
        $this->assertDatabaseMissing('wallet_user', [
            'wallet_id' => $this->ownerWallet()->id,
            'user_id'   => $this->member->id,
        ]);
    }

    public function test_cannot_accept_invitation_for_other_email(): void
    {
        $invitation = $this->invite();
        $stranger   = User::factory()->create();

        $this->actingAs($stranger)
             ->postJson("/api/invitations/{$invitation->token}/accept")
             ->assertStatus(404);
    }

    public function test_cannot_accept_expired_invitation(): void
    {
        $invitation = $this->invite();
        $invitation->update(['expires_at' => now()->subDay()]);

        $this->actingAs($this->member)
             ->postJson("/api/invitations/{$invitation->token}/accept")
             ->assertStatus(422);
    }

    // --- Access after accept ---

    public function test_member_sees_shared_wallet_in_index(): void
    {
        $this->ownerWallet()->members()->attach($this->member->id, ['role' => 'member']);

        $response = $this->actingAs($this->member)->getJson('/api/wallets')->assertOk();

        $ids = collect($response->json('data'))->pluck('id');
        $this->assertTrue($ids->contains($this->ownerWallet()->id));
    }

    public function test_member_can_create_transaction_on_shared_wallet(): void
    {
        $wallet = $this->ownerWallet();
        $wallet->members()->attach($this->member->id, ['role' => 'member']);
        $category = $this->member->categories()->where('type', 'expense')->first();

        $this->actingAs($this->member)->postJson('/api/transactions', [
            'category_id' => $category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Belanja bareng',
            'amount'      => 30000,
            'type'        => 'expense',
            'date'        => '2026-07-13',
        ])->assertStatus(201);

        $this->assertEquals(-30000, $wallet->fresh()->balance);
    }

    public function test_non_member_cannot_create_transaction_on_wallet(): void
    {
        $wallet   = $this->ownerWallet(); // owner's wallet, member NOT attached
        $category = $this->member->categories()->where('type', 'expense')->first();

        $this->actingAs($this->member)->postJson('/api/transactions', [
            'category_id' => $category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Nyolong',
            'amount'      => 30000,
            'type'        => 'expense',
            'date'        => '2026-07-13',
        ])->assertStatus(422);
    }

    // --- Member permissions ---

    public function test_member_cannot_edit_shared_wallet(): void
    {
        $wallet = $this->ownerWallet();
        $wallet->members()->attach($this->member->id, ['role' => 'member']);

        $this->actingAs($this->member)->putJson("/api/wallets/{$wallet->id}", [
            'name' => 'Diambil alih',
        ])->assertStatus(403);
    }

    public function test_member_cannot_remove_members(): void
    {
        $wallet = $this->ownerWallet();
        $wallet->members()->attach($this->member->id, ['role' => 'member']);

        $this->actingAs($this->member)
             ->deleteJson("/api/wallets/{$wallet->id}/members/{$this->member->id}")
             ->assertStatus(403);
    }

    // --- Owner member management ---

    public function test_owner_can_list_members(): void
    {
        $wallet = $this->ownerWallet();
        $wallet->members()->attach($this->member->id, ['role' => 'member']);

        $response = $this->actingAs($this->owner)
                         ->getJson("/api/wallets/{$wallet->id}/members")
                         ->assertOk();

        $this->assertEquals($this->owner->id, $response->json('data.owner.id'));
        $this->assertCount(1, $response->json('data.members'));
    }

    public function test_owner_can_remove_member(): void
    {
        $wallet = $this->ownerWallet();
        $wallet->members()->attach($this->member->id, ['role' => 'member']);

        $this->actingAs($this->owner)
             ->deleteJson("/api/wallets/{$wallet->id}/members/{$this->member->id}")
             ->assertOk();

        $this->assertDatabaseMissing('wallet_user', [
            'wallet_id' => $wallet->id,
            'user_id'   => $this->member->id,
        ]);
    }
}
