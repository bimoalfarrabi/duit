<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\CategoryResource;
use App\Models\Category;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CategoryController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $categories = $request->user()->categories()->get();

        return $this->success(CategoryResource::collection($categories));
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'name'  => 'required|string|max:255',
            'type'  => 'required|in:income,expense',
            'color' => 'required|string|size:7',
            'icon'  => 'required|string|max:50',
        ]);

        $category = $request->user()->categories()->create($data);

        return $this->success(new CategoryResource($category), 'Kategori dibuat', 201);
    }

    public function update(Request $request, Category $category): JsonResponse
    {
        if ($category->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        $data = $request->validate([
            'name'  => 'sometimes|string|max:255',
            'type'  => 'sometimes|in:income,expense',
            'color' => 'sometimes|string|size:7',
            'icon'  => 'sometimes|string|max:50',
        ]);

        $category->update($data);

        return $this->success(new CategoryResource($category));
    }

    public function destroy(Request $request, Category $category): JsonResponse
    {
        if ($category->user_id !== $request->user()->id) {
            return $this->error('Forbidden', 403);
        }

        $category->delete();

        return $this->success(null, 'Kategori dihapus');
    }
}
