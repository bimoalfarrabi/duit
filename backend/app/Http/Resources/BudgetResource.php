<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class BudgetResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id'          => $this->id,
            'category_id' => $this->category_id,
            'category'    => new CategoryResource($this->whenLoaded('category')),
            'month'       => $this->month,
            'year'        => $this->year,
            'amount'      => $this->amount,
            'spent'       => $this->spent ?? 0, // ponytail: di-inject dari controller, bukan relasi
        ];
    }
}
