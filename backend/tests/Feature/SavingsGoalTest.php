<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class SavingsGoalTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    protected function setUp(): void
    {
        parent::setUp();
        $this->user = User::factory()->create();
    }

    public function test_can_list_savings_goals(): void
    {
        $this->user->savingsGoals()->create([
            'name'          => 'Laptop baru',
            'target_amount' => 10000000,
        ]);

        $this->actingAs($this->user)
            ->getJson('/api/savings')
            ->assertOk()
            ->assertJsonCount(1, 'data')
            ->assertJsonPath('data.0.name', 'Laptop baru');
    }

    public function test_can_create_savings_goal(): void
    {
        $this->actingAs($this->user)
            ->postJson('/api/savings', [
                'name'          => 'Liburan Bali',
                'target_amount' => 5000000,
            ])
            ->assertStatus(201)
            ->assertJsonPath('data.name', 'Liburan Bali')
            ->assertJsonPath('data.is_completed', false);
    }

    public function test_can_topup_savings_goal(): void
    {
        $goal = $this->user->savingsGoals()->create([
            'name'          => 'Laptop baru',
            'target_amount' => 10000000,
        ]);

        $this->actingAs($this->user)
            ->putJson("/api/savings/{$goal->id}", [
                'current_amount' => 3000000,
            ])
            ->assertOk()
            ->assertJsonPath('data.current_amount', '3000000.00');
    }

    public function test_auto_complete_when_target_reached(): void
    {
        $goal = $this->user->savingsGoals()->create([
            'name'          => 'Laptop baru',
            'target_amount' => 1000000,
        ]);

        $this->actingAs($this->user)
            ->putJson("/api/savings/{$goal->id}", [
                'current_amount' => 1000000,
            ])
            ->assertOk()
            ->assertJsonPath('data.is_completed', true);
    }

    public function test_can_delete_savings_goal(): void
    {
        $goal = $this->user->savingsGoals()->create([
            'name'          => 'Laptop baru',
            'target_amount' => 10000000,
        ]);

        $this->actingAs($this->user)
            ->deleteJson("/api/savings/{$goal->id}")
            ->assertOk();

        $this->assertDatabaseMissing('savings_goals', ['id' => $goal->id]);
    }

    public function test_cannot_update_other_user_goal(): void
    {
        $other     = User::factory()->create();
        $otherGoal = $other->savingsGoals()->create([
            'name'          => 'Hacked',
            'target_amount' => 1000,
        ]);

        $this->actingAs($this->user)
            ->putJson("/api/savings/{$otherGoal->id}", [
                'current_amount' => 999,
            ])
            ->assertStatus(403);
    }
}
