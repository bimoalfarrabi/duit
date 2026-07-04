<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class TransactionTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    protected function setUp(): void
    {
        parent::setUp();
        $this->user = User::factory()->create();
    }

    private function wallet()
    {
        return $this->user->wallets()->where('type', 'cash')->first();
    }

    private function category(string $type = 'expense')
    {
        return $this->user->categories()->where('type', $type)->first();
    }

    public function test_create_transaction_updates_wallet_balance(): void
    {
        $wallet   = $this->wallet();
        $category = $this->category('expense');

        $this->actingAs($this->user)->postJson('/api/transactions', [
            'category_id' => $category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Makan siang',
            'amount'      => 25000,
            'type'        => 'expense',
            'date'        => '2026-07-04',
        ])->assertStatus(201);

        $this->assertEquals(-25000, $wallet->fresh()->balance);
    }

    public function test_create_income_transaction_increases_balance(): void
    {
        $wallet   = $this->wallet();
        $category = $this->category('income');

        $this->actingAs($this->user)->postJson('/api/transactions', [
            'category_id' => $category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Gajian',
            'amount'      => 5000000,
            'type'        => 'income',
            'date'        => '2026-07-04',
        ])->assertStatus(201);

        $this->assertEquals(5000000, $wallet->fresh()->balance);
    }

    public function test_delete_transaction_reverses_balance(): void
    {
        $wallet   = $this->wallet();
        $category = $this->category('expense');

        $response = $this->actingAs($this->user)->postJson('/api/transactions', [
            'category_id' => $category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Test',
            'amount'      => 10000,
            'type'        => 'expense',
            'date'        => '2026-07-04',
        ]);

        $id = $response->json('data.id');

        $this->actingAs($this->user)->deleteJson("/api/transactions/{$id}")
             ->assertOk();

        $this->assertEquals(0, $wallet->fresh()->balance);
    }

    public function test_filter_by_month_and_year(): void
    {
        $wallet   = $this->wallet();
        $category = $this->category('expense');

        $this->actingAs($this->user)->postJson('/api/transactions', [
            'category_id' => $category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Juli',
            'amount'      => 10000,
            'type'        => 'expense',
            'date'        => '2026-07-01',
        ]);

        $this->actingAs($this->user)->postJson('/api/transactions', [
            'category_id' => $category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Juni',
            'amount'      => 5000,
            'type'        => 'expense',
            'date'        => '2026-06-01',
        ]);

        $response = $this->actingAs($this->user)
                         ->getJson('/api/transactions?month=7&year=2026');

        $response->assertOk();
        $this->assertCount(1, $response->json('data'));
        $this->assertEquals('Juli', $response->json('data.0.title'));
    }

    public function test_ownership_check_on_delete(): void
    {
        $other    = User::factory()->create();
        $wallet   = $other->wallets()->where('type', 'cash')->first();
        $category = $other->categories()->where('type', 'expense')->first();

        $tx = $other->transactions()->create([
            'category_id' => $category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Orang lain',
            'amount'      => 5000,
            'type'        => 'expense',
            'date'        => '2026-07-04',
        ]);

        $this->actingAs($this->user)->deleteJson("/api/transactions/{$tx->id}")
             ->assertStatus(403);
    }
}
