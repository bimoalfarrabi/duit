<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\TransactionResource;
use App\Models\Transaction;
use App\Models\Wallet;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class TransactionController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $query = $request->user()->transactions()->with(['category', 'wallet']);

        if ($request->filled('month')) {
            $query->whereMonth('date', $request->integer('month'));
        }
        if ($request->filled('year')) {
            $query->whereYear('date', $request->integer('year'));
        }
        if ($request->filled('type')) {
            $query->where('type', $request->input('type'));
        }
        if ($request->filled('category_id')) {
            $query->where('category_id', $request->integer('category_id'));
        }
        if ($request->filled('wallet_id')) {
            $query->where('wallet_id', $request->integer('wallet_id'));
        }

        $transactions = $query->orderByDesc('date')->orderByDesc('id')->paginate(20);

        return $this->success(TransactionResource::collection($transactions));
    }

    public function store(Request $request): JsonResponse
    {
        $userId = $request->user()->id;
        $data = $request->validate([
            'category_id' => ['required', Rule::exists('categories', 'id')->where('user_id', $userId)],
            'wallet_id'   => ['required', Rule::exists('wallets', 'id')->where('user_id', $userId)],
            'title'       => 'required|string|max:255',
            'amount'      => 'required|numeric|min:0',
            'type'        => 'required|in:income,expense',
            'date'        => 'required|date',
            'note'        => 'nullable|string',
        ]);

        $data['user_id'] = $request->user()->id;

        $transaction = Transaction::create($data);

        $wallet = Wallet::findOrFail($data['wallet_id']);
        $delta  = $data['type'] === 'income' ? $data['amount'] : -$data['amount'];
        $wallet->increment('balance', $delta);

        return $this->success(
            new TransactionResource($transaction->load(['category', 'wallet'])),
            'Transaksi dibuat',
            201
        );
    }

    public function update(Request $request, Transaction $transaction): JsonResponse
    {
        if ($transaction->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        $userId = $request->user()->id;
        $data = $request->validate([
            'category_id' => ['sometimes', Rule::exists('categories', 'id')->where('user_id', $userId)],
            'wallet_id'   => ['sometimes', Rule::exists('wallets', 'id')->where('user_id', $userId)],
            'title'       => 'sometimes|string|max:255',
            'amount'      => 'sometimes|numeric|min:0',
            'type'        => 'sometimes|in:income,expense',
            'date'        => 'sometimes|date',
            'note'        => 'nullable|string',
        ]);

        // Reverse old balance
        $oldWallet = Wallet::findOrFail($transaction->wallet_id);
        $oldDelta  = $transaction->type === 'income' ? $transaction->amount : -$transaction->amount;
        $oldWallet->decrement('balance', $oldDelta);

        $transaction->update($data);
        $transaction->refresh();

        // Apply new balance
        $newWallet = Wallet::findOrFail($transaction->wallet_id);
        $newDelta  = $transaction->type === 'income' ? $transaction->amount : -$transaction->amount;
        $newWallet->increment('balance', $newDelta);

        return $this->success(
            new TransactionResource($transaction->load(['category', 'wallet']))
        );
    }

    public function destroy(Request $request, Transaction $transaction): JsonResponse
    {
        if ($transaction->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        // Reverse balance
        $wallet = Wallet::findOrFail($transaction->wallet_id);
        $delta  = $transaction->type === 'income' ? $transaction->amount : -$transaction->amount;
        $wallet->decrement('balance', $delta);

        $transaction->delete();

        return $this->success(null, 'Transaksi dihapus');
    }
}
