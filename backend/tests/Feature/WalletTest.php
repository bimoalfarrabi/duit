<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class WalletTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    protected function setUp(): void
    {
        parent::setUp();
        $this->user = User::factory()->create();
    }

    public function test_cannot_create_second_cash_wallet(): void
    {
        // Cash wallet already created by UserObserver on user creation
        $this->actingAs($this->user)->postJson('/api/wallets', [
            'name'  => 'Cash 2',
            'type'  => 'cash',
            'color' => '#000000',
            'icon'  => 'wallet',
        ])->assertStatus(422);
    }

    public function test_can_create_bank_wallet(): void
    {
        $this->actingAs($this->user)->postJson('/api/wallets', [
            'name'  => 'BCA',
            'type'  => 'bank',
            'color' => '#1565C0',
            'icon'  => 'bank',
        ])->assertStatus(201)
          ->assertJsonPath('data.type', 'bank');
    }

    public function test_cannot_delete_cash_wallet(): void
    {
        $cashWallet = $this->user->wallets()->where('type', 'cash')->first();

        $this->actingAs($this->user)->deleteJson("/api/wallets/{$cashWallet->id}")
             ->assertStatus(403);
    }

    public function test_can_delete_bank_wallet(): void
    {
        $wallet = $this->user->wallets()->create([
            'name' => 'BCA', 'type' => 'bank', 'color' => '#000', 'icon' => 'bank',
        ]);

        $this->actingAs($this->user)->deleteJson("/api/wallets/{$wallet->id}")
             ->assertOk();
    }

    public function test_user_cannot_update_other_user_wallet(): void
    {
        $other      = User::factory()->create();
        $otherWallet = $other->wallets()->where('type', 'cash')->first();

        $this->actingAs($this->user)->putJson("/api/wallets/{$otherWallet->id}", [
            'name' => 'Hacked',
        ])->assertStatus(403);
    }
}
