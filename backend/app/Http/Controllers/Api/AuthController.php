<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\UserResource;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Log;

class AuthController extends Controller
{
    public function register(Request $request): JsonResponse
    {
        $data = $request->validate([
            'name'     => 'required|string|max:255',
            'email'    => 'required|email|unique:users,email',
            'password' => ['required', 'string', 'min:8', 'regex:/[A-Z]/', 'regex:/[0-9]/'],
        ]);

        $user  = User::create($data);
        $token = $user->createToken('auth_token')->plainTextToken;

        return $this->success([
            'token' => $token,
            'user'  => new UserResource($user),
        ], 'Register berhasil', 201);
    }

    public function login(Request $request): JsonResponse
    {
        $data = $request->validate([
            'email'    => 'required|email',
            'password' => 'required|string',
        ]);

        if (!Auth::attempt($data)) {
            Log::warning('failed_login', ['email' => $data['email'], 'ip' => $request->ip()]);
            return $this->error('Email atau password salah', 401);
        }

        $user = Auth::user();

        // Jika 2FA aktif, return temp token — client harus lanjut ke /two-factor-challenge
        if ($user->two_factor_confirmed_at) {
            $tempToken = $user->createToken('2fa-temp', ['2fa-challenge'], now()->addMinutes(5))->plainTextToken;
            Log::info('login_2fa_required', ['user_id' => $user->id, 'ip' => $request->ip()]);

            return $this->success([
                'requires_2fa' => true,
                'temp_token'   => $tempToken,
            ], '2FA diperlukan');
        }

        $token = $user->createToken('auth_token')->plainTextToken;

        Log::info('login_success', ['user_id' => $user->id, 'ip' => $request->ip()]);

        return $this->success([
            'token' => $token,
            'user'  => new UserResource($user),
        ]);
    }

    public function logout(Request $request): JsonResponse
    {
        $request->user()->currentAccessToken()->delete();

        return $this->success(null, 'Logout berhasil');
    }

    public function me(Request $request): JsonResponse
    {
        return $this->success(new UserResource($request->user()));
    }
}
