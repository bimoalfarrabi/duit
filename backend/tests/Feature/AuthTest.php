<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class AuthTest extends TestCase
{
    use RefreshDatabase;

    public function test_register_creates_user_wallet_and_categories(): void
    {
        $response = $this->postJson('/api/auth/register', [
            'name'     => 'Budi',
            'email'    => 'budi@mail.com',
            'password' => 'Secret123',
        ]);

        $response->assertStatus(201)
                 ->assertJsonPath('status', true)
                 ->assertJsonStructure(['data' => ['token', 'user']]);

        $user = User::where('email', 'budi@mail.com')->first();
        $this->assertNotNull($user);
        $this->assertCount(1, $user->wallets()->where('type', 'cash')->get());
        $this->assertCount(8, $user->categories);
    }

    public function test_login_returns_token(): void
    {
        $user = User::factory()->create(['password' => bcrypt('secret123')]);

        $response = $this->postJson('/api/auth/login', [
            'email'    => $user->email,
            'password' => 'secret123',
        ]);

        $response->assertOk()
                 ->assertJsonPath('status', true)
                 ->assertJsonStructure(['data' => ['token', 'user']]);
    }

    public function test_login_fails_with_wrong_password(): void
    {
        $user = User::factory()->create(['password' => bcrypt('secret123')]);

        $this->postJson('/api/auth/login', [
            'email'    => $user->email,
            'password' => 'wrong',
        ])->assertStatus(401);
    }

    public function test_me_requires_auth(): void
    {
        $this->getJson('/api/auth/me')->assertStatus(401);
    }

    public function test_me_returns_user(): void
    {
        $user = User::factory()->create();

        $this->actingAs($user)->getJson('/api/auth/me')
             ->assertOk()
             ->assertJsonPath('data.email', $user->email);
    }

    public function test_logout_revokes_token(): void
    {
        $user  = User::factory()->create();
        $token = $user->createToken('test')->plainTextToken;

        $this->withToken($token)->postJson('/api/auth/logout')
             ->assertOk();

        // Token must no longer exist in DB — verify directly
        $this->assertDatabaseMissing('personal_access_tokens', [
            'tokenable_id' => $user->id,
        ]);
    }
}
