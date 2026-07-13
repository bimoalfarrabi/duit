<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\WalletResource;
use App\Models\Wallet;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class WalletController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $wallets = $request->user()->wallets()->get()
            ->concat($request->user()->sharedWallets()->get());

        return $this->success(WalletResource::collection($wallets));
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'name'  => 'required|string|max:255',
            'type'  => 'required|in:cash,bank,ewallet',
            'color' => 'required|string|size:7',
            'icon'  => 'required|string|max:50',
        ]);

        if ($data['type'] === 'cash' && $request->user()->wallets()->where('type', 'cash')->exists()) {
            return $this->error('Cash wallet sudah ada', 422);
        }

        $wallet = $request->user()->wallets()->create($data);

        return $this->success(new WalletResource($wallet), 'Wallet dibuat', 201);
    }

    public function update(Request $request, Wallet $wallet): JsonResponse
    {
        if ($wallet->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        $data = $request->validate([
            'name'  => 'sometimes|string|max:255',
            'color' => 'sometimes|string|size:7',
            'icon'  => 'sometimes|string|max:50',
        ]);

        $wallet->update($data);

        return $this->success(new WalletResource($wallet));
    }

    public function destroy(Request $request, Wallet $wallet): JsonResponse
    {
        if ($wallet->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        if ($wallet->type === 'cash') {
            return $this->error('Cash wallet tidak bisa dihapus', 403);
        }

        $wallet->delete();

        return $this->success(null, 'Wallet dihapus');
    }
}
