<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\WalletResource;
use App\Models\Transaction;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class StatisticsController extends Controller
{
    public function summary(Request $request): JsonResponse
    {
        $month = $request->integer('month', now()->month);
        $year  = $request->integer('year', now()->year);

        $result = $request->user()->transactions()
            ->whereMonth('date', $month)
            ->whereYear('date', $year)
            ->selectRaw("
                SUM(CASE WHEN type = 'income'  THEN amount ELSE 0 END) as income,
                SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END) as expense
            ")
            ->first();

        $income  = (float) ($result->income ?? 0);
        $expense = (float) ($result->expense ?? 0);

        return $this->success([
            'income'  => $income,
            'expense' => $expense,
            'balance' => $income - $expense,
        ]);
    }

    public function byCategory(Request $request): JsonResponse
    {
        $month = $request->integer('month', now()->month);
        $year  = $request->integer('year', now()->year);

        $query = $request->user()->transactions()
            ->whereMonth('date', $month)
            ->whereYear('date', $year)
            ->join('categories', 'transactions.category_id', '=', 'categories.id')
            ->selectRaw('
                transactions.category_id,
                categories.name as category_name,
                transactions.type,
                SUM(transactions.amount) as total
            ')
            ->groupBy('transactions.category_id', 'categories.name', 'transactions.type');

        if ($request->filled('type')) {
            $query->where('transactions.type', $request->input('type'));
        }

        $rows  = $query->get();
        $grand = $rows->sum('total') ?: 1;

        $data = $rows->map(fn($row) => [
            'category_id'   => $row->category_id,
            'category_name' => $row->category_name,
            'type'          => $row->type,
            'total'         => (float) $row->total,
            'percentage'    => round((float) $row->total / $grand * 100, 2),
        ]);

        return $this->success($data);
    }

    public function byWallet(Request $request): JsonResponse
    {
        $wallets = $request->user()->wallets()->get();

        return $this->success(WalletResource::collection($wallets));
    }

    public function monthly(Request $request): JsonResponse
    {
        $rows = $request->user()->transactions()
            ->where('date', '>=', now()->subMonths(11)->startOfMonth())
            ->selectRaw("
                YEAR(date)  as year,
                MONTH(date) as month,
                SUM(CASE WHEN type = 'income'  THEN amount ELSE 0 END) as income,
                SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END) as expense
            ")
            ->groupByRaw('YEAR(date), MONTH(date)')
            ->orderByRaw('YEAR(date), MONTH(date)')
            ->get()
            ->map(fn($row) => [
                'year'    => (int) $row->year,
                'month'   => (int) $row->month,
                'income'  => (float) $row->income,
                'expense' => (float) $row->expense,
            ]);

        return $this->success($rows);
    }
}
