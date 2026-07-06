<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\SavingsGoalResource;
use App\Models\SavingsGoal;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class SavingsGoalController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $goals = $request->user()->savingsGoals()->orderBy('created_at', 'desc')->get();

        return $this->success(SavingsGoalResource::collection($goals));
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'name'          => 'required|string|max:255',
            'target_amount' => 'required|numeric|min:0',
            'deadline'      => 'nullable|date|after:today',
        ]);

        $goal = $request->user()->savingsGoals()->create($data);
        $goal->refresh(); // ponytail: cast tidak diapply pada model baru sebelum refresh

        return $this->success(new SavingsGoalResource($goal), 'Target tabungan dibuat', 201);
    }

    public function update(Request $request, SavingsGoal $saving): JsonResponse
    {
        if ($saving->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        $data = $request->validate([
            'name'           => 'sometimes|string|max:255',
            'target_amount'  => 'sometimes|numeric|min:0',
            'current_amount' => 'sometimes|numeric|min:0',
            'deadline'       => 'sometimes|nullable|date',
            'is_completed'   => 'sometimes|boolean',
        ]);

        $saving->update($data);

        // ponytail: auto-complete jika current_amount >= target_amount
        if ($saving->current_amount >= $saving->target_amount) {
            $saving->update(['is_completed' => true]);
        }

        return $this->success(new SavingsGoalResource($saving));
    }

    public function destroy(Request $request, SavingsGoal $saving): JsonResponse
    {
        if ($saving->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        $saving->delete();

        return $this->success(null, 'Target tabungan dihapus');
    }
}
