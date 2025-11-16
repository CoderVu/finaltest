# Plan: Refactoring Element Package

## Status: IN PROGRESS

Đây là một refactoring lớn, cần làm từng bước để tránh break code hiện tại.

## Approach

**Option A: Big Bang (Không khuyến nghị)**
- Tạo tất cả file mới
- Xóa Element.java cũ
- Update tất cả imports
- Risk: High - có thể break nhiều thứ

**Option B: Incremental (Khuyến nghị)**
1. Tạo cấu trúc mới song song với cũ
2. Migrate từng component một
3. Update imports dần dần
4. Xóa code cũ sau khi đã migrate hết

## Current Progress

✅ Created:
- IElement.java (interface)
- LocatorResolver.java

⏳ To Do:
- BaseElement.java
- Component classes (ElementActions, ElementWaits, etc.)
- WebElementWrapper.java
- Move ElementFactory to factory/
- Update all imports

## Recommendation

Nên làm theo Option B (Incremental) để đảm bảo code vẫn chạy được trong quá trình refactor.

