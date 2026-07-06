<?php

namespace Tests\Feature;

use App\Models\Category;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class BudgetTest extends TestCase
{
    use RefreshDatabase;

    private User $user;
    private Category $category;

    protected function setUp(): void
    {
        parent::setUp();
        $this->user = User::factory()->create();
        $this->category = $this->user->categories()->create([
            'name' => 'Makan', 'type' => 'expense', 'color' => '#FF0000', 'icon' => 'food',
        ]);
    }

    public function test_can_list_budgets_for_current_month(): void
    {
        $this->user->budgets()->create([
            'category_id' => $this->category->id,
            'month'       => now()->month,
            'year'        => now()->year,
            'amount'      => 500000,
        ]);

        $this->actingAs($this->user)
            ->getJson('/api/budgets')
            ->assertOk()
            ->assertJsonCount(1, 'data')
            ->assertJsonPath('data.0.amount', '500000.00');
    }

    public function test_can_create_budget(): void
    {
        $this->actingAs($this->user)
            ->postJson('/api/budgets', [
                'category_id' => $this->category->id,
                'month'       => 8,
                'year'        => 2026,
                'amount'      => 750000,
            ])
            ->assertStatus(201)
            ->assertJsonPath('data.amount', '750000.00');
    }

    public function test_post_budget_upserts_existing(): void
    {
        $this->user->budgets()->create([
            'category_id' => $this->category->id,
            'month'       => 8,
            'year'        => 2026,
            'amount'      => 500000,
        ]);

        $this->actingAs($this->user)
            ->postJson('/api/budgets', [
                'category_id' => $this->category->id,
                'month'       => 8,
                'year'        => 2026,
                'amount'      => 999000,
            ])
            ->assertStatus(201)
            ->assertJsonPath('data.amount', '999000.00');

        $this->assertDatabaseCount('budgets', 1);
    }

    public function test_cannot_create_budget_for_other_user_category(): void
    {
        $other         = User::factory()->create();
        $otherCategory = $other->categories()->create([
            'name' => 'X', 'type' => 'expense', 'color' => '#000', 'icon' => 'x',
        ]);

        $this->actingAs($this->user)
            ->postJson('/api/budgets', [
                'category_id' => $otherCategory->id,
                'month'       => 8,
                'year'        => 2026,
                'amount'      => 100000,
            ])
            ->assertStatus(404);
    }

    public function test_can_delete_budget(): void
    {
        $budget = $this->user->budgets()->create([
            'category_id' => $this->category->id,
            'month'       => 8,
            'year'        => 2026,
            'amount'      => 500000,
        ]);

        $this->actingAs($this->user)
            ->deleteJson("/api/budgets/{$budget->id}")
            ->assertOk();

        $this->assertDatabaseMissing('budgets', ['id' => $budget->id]);
    }

    public function test_cannot_delete_other_user_budget(): void
    {
        $other         = User::factory()->create();
        $otherCategory = $other->categories()->create([
            'name' => 'X', 'type' => 'expense', 'color' => '#000', 'icon' => 'x',
        ]);
        $otherBudget = $other->budgets()->create([
            'category_id' => $otherCategory->id,
            'month'       => 8,
            'year'        => 2026,
            'amount'      => 100000,
        ]);

        $this->actingAs($this->user)
            ->deleteJson("/api/budgets/{$otherBudget->id}")
            ->assertStatus(403);
    }

    public function test_budget_index_includes_spent(): void
    {
        $this->user->budgets()->create([
            'category_id' => $this->category->id,
            'month'       => now()->month,
            'year'        => now()->year,
            'amount'      => 500000,
        ]);

        $wallet = $this->user->wallets()->where('type', 'cash')->first();
        $this->user->transactions()->create([
            'category_id' => $this->category->id,
            'wallet_id'   => $wallet->id,
            'title'       => 'Makan siang',
            'amount'      => 25000,
            'type'        => 'expense',
            'date'        => now()->toDateString(),
        ]);

        $response = $this->actingAs($this->user)
            ->getJson('/api/budgets')
            ->assertOk();

        $this->assertEquals(25000, $response->json('data.0.spent'));
    }
}
