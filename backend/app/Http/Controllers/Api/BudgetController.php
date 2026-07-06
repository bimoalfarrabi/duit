<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\BudgetResource;
use App\Models\Budget;
use App\Models\Transaction;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class BudgetController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $month = (int) $request->query('month', now()->month);
        $year  = (int) $request->query('year', now()->year);

        $budgets = $request->user()->budgets()
            ->with('category')
            ->where('month', $month)
            ->where('year', $year)
            ->get();

        // ponytail: inject spent via subquery per budget, avoid N+1
        $categoryIds = $budgets->pluck('category_id');
        $spent = Transaction::query()
            ->where('user_id', $request->user()->id)
            ->where('type', 'expense')
            ->whereIn('category_id', $categoryIds)
            ->whereMonth('date', $month)
            ->whereYear('date', $year)
            ->selectRaw('category_id, SUM(amount) as total')
            ->groupBy('category_id')
            ->pluck('total', 'category_id');

        $budgets->each(fn ($b) => $b->spent = $spent[$b->category_id] ?? 0);

        return $this->success(BudgetResource::collection($budgets));
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'category_id' => 'required|integer|exists:categories,id',
            'month'       => 'required|integer|between:1,12',
            'year'        => 'required|integer|min:2000|max:2100',
            'amount'      => 'required|numeric|min:0',
        ]);

        // ownership check: kategori harus milik user
        $category = $request->user()->categories()->findOrFail($data['category_id']);

        // ponytail: upsert — POST juga untuk update, tidak perlu PUT endpoint
        $budget = $request->user()->budgets()->updateOrCreate(
            ['category_id' => $data['category_id'], 'month' => $data['month'], 'year' => $data['year']],
            ['amount' => $data['amount']]
        );

        $budget->load('category');
        $budget->spent = 0;

        return $this->success(new BudgetResource($budget), 'Budget disimpan', 201);
    }

    public function destroy(Request $request, Budget $budget): JsonResponse
    {
        if ($budget->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        $budget->delete();

        return $this->success(null, 'Budget dihapus');
    }
}
