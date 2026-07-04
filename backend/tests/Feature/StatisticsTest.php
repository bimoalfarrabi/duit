<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class StatisticsTest extends TestCase
{
    use RefreshDatabase;

    public function test_summary_returns_correct_income_and_expense(): void
    {
        $user     = User::factory()->create();
        $wallet   = $user->wallets()->where('type', 'cash')->first();
        $income   = $user->categories()->where('type', 'income')->first();
        $expense  = $user->categories()->where('type', 'expense')->first();

        $user->transactions()->createMany([
            ['category_id' => $income->id,  'wallet_id' => $wallet->id, 'title' => 'Gaji',  'amount' => 5000000, 'type' => 'income',  'date' => '2026-07-01'],
            ['category_id' => $expense->id, 'wallet_id' => $wallet->id, 'title' => 'Makan', 'amount' => 200000,  'type' => 'expense', 'date' => '2026-07-02'],
            // Different month — should NOT appear in July summary
            ['category_id' => $income->id,  'wallet_id' => $wallet->id, 'title' => 'Bonus', 'amount' => 1000000, 'type' => 'income',  'date' => '2026-06-01'],
        ]);

        $response = $this->actingAs($user)->getJson('/api/statistics/summary?month=7&year=2026');

        $response->assertOk()
                 ->assertJsonPath('data.income',  5000000)
                 ->assertJsonPath('data.expense', 200000)
                 ->assertJsonPath('data.balance', 4800000);
    }
}
