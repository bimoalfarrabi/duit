<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class SavingsGoalResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id'             => $this->id,
            'name'           => $this->name,
            'target_amount'  => $this->target_amount,
            'current_amount' => $this->current_amount,
            'deadline'       => $this->deadline?->toDateString(),
            'is_completed'   => $this->is_completed,
        ];
    }
}
