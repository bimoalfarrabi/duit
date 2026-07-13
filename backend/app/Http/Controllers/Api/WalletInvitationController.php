<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\WalletInvitationResource;
use App\Models\Wallet;
use App\Models\WalletInvitation;
use App\Notifications\WalletInvitationNotification;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Str;

class WalletInvitationController extends Controller
{
    /** Owner mengundang member baru via email. */
    public function store(Request $request, Wallet $wallet): JsonResponse
    {
        if (! $wallet->isOwnedBy($request->user())) {
            return $this->error('Forbidden', 403);
        }

        $data = $request->validate([
            'email' => 'required|email',
        ]);

        $email = $data['email'];

        if ($email === $request->user()->email) {
            return $this->error('Tidak bisa mengundang diri sendiri', 422);
        }

        // Sudah jadi member?
        if ($wallet->members()->where('email', $email)->exists()) {
            return $this->error('User sudah menjadi member wallet ini', 422);
        }

        // Sudah ada undangan pending?
        if ($wallet->invitations()->where('email', $email)->where('status', 'pending')->exists()) {
            return $this->error('Undangan untuk email ini masih pending', 422);
        }

        $invitation = $wallet->invitations()->create([
            'inviter_id' => $request->user()->id,
            'email'      => $email,
            'token'      => Str::random(64), // ponytail: plain token 64-char, cukup untuk invite wallet
            'status'     => 'pending',
            'expires_at' => now()->addDays(7),
        ]);

        Notification::route('mail', $email)
            ->notify(new WalletInvitationNotification($invitation));

        return $this->success(
            new WalletInvitationResource($invitation->load(['wallet', 'inviter'])),
            'Undangan dikirim',
            201
        );
    }

    /** Undangan pending yang ditujukan ke user yang login. */
    public function index(Request $request): JsonResponse
    {
        $invitations = WalletInvitation::query()
            ->with(['wallet', 'inviter'])
            ->where('email', $request->user()->email)
            ->where('status', 'pending')
            ->where('expires_at', '>', now())
            ->latest()
            ->get();

        return $this->success(WalletInvitationResource::collection($invitations));
    }

    public function accept(Request $request, string $token): JsonResponse
    {
        $invitation = $this->pendingInvitationFor($request, $token);

        if ($invitation instanceof JsonResponse) {
            return $invitation;
        }

        // Idempotent: hanya attach jika belum member
        if (! $invitation->wallet->members()->whereKey($request->user()->id)->exists()) {
            $invitation->wallet->members()->attach($request->user()->id, ['role' => 'member']);
        }

        $invitation->update(['status' => 'accepted']);

        return $this->success(null, 'Undangan diterima');
    }

    public function decline(Request $request, string $token): JsonResponse
    {
        $invitation = $this->pendingInvitationFor($request, $token);

        if ($invitation instanceof JsonResponse) {
            return $invitation;
        }

        $invitation->update(['status' => 'declined']);

        return $this->success(null, 'Undangan ditolak');
    }

    /** Ambil undangan pending milik user, atau JsonResponse error. */
    private function pendingInvitationFor(Request $request, string $token): WalletInvitation|JsonResponse
    {
        $invitation = WalletInvitation::where('token', $token)->first();

        if ($invitation === null || $invitation->email !== $request->user()->email) {
            return $this->error('Undangan tidak ditemukan', 404);
        }

        if ($invitation->status !== 'pending') {
            return $this->error('Undangan sudah tidak berlaku', 422);
        }

        if ($invitation->isExpired()) {
            return $this->error('Undangan sudah kedaluwarsa', 422);
        }

        return $invitation;
    }
}
