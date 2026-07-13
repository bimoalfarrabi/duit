<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Wallet;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class WalletMemberController extends Controller
{
    /** Daftar member wallet (owner + shared members). Bisa diakses siapa saja yang punya akses. */
    public function index(Request $request, Wallet $wallet): JsonResponse
    {
        if (! $wallet->isAccessibleBy($request->user())) {
            return $this->error('Forbidden', 403);
        }

        $members = $wallet->members()->get()->map(fn (User $u) => [
            'id'    => $u->id,
            'name'  => $u->name,
            'email' => $u->email,
            'role'  => $u->pivot->role,
        ]);

        return $this->success([
            'owner' => [
                'id'    => $wallet->user->id,
                'name'  => $wallet->user->name,
                'email' => $wallet->user->email,
                'role'  => 'owner',
            ],
            'members' => $members,
        ]);
    }

    /** Hapus member. Owner-only. */
    public function destroy(Request $request, Wallet $wallet, User $member): JsonResponse
    {
        if (! $wallet->isOwnedBy($request->user())) {
            return $this->error('Forbidden', 403);
        }

        $wallet->members()->detach($member->id);

        return $this->success(null, 'Member dihapus');
    }
}
