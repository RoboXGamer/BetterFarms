# Better Farms - 5 Farm Blocks Implementation Checklist

## Phase 1: Refactor Crop Farm & Establish Base Abstractions

-   **[x] Create Abstract Base Classes:**
    -   **[x] `AbstractFarmBlock.java`:**
        -   Extend `net.minecraft.world.level.block.Block`.
        -   Implement `net.minecraft.world.level.block.EntityBlock`.
        -   Add constructor taking `BlockBehaviour.Properties`.
        -   Implement `useWithoutItem` to open the menu (similar to `CropFarmBlock`).
        -   Implement `onRemove` to call `drops()` on the Block Entity.
        -   Implement a potentially generic `getTicker` or leave it for subclasses if logic differs slightly.
        -   Declare `public abstract BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState);`
    -   **[x] `AbstractFarmBlockEntity.java`:**
        -   Extend `net.minecraft.world.level.block.entity.BlockEntity`.
        -   Implement `net.minecraft.world.inventory.MenuProvider`.
        -   **[x] Common Fields:**
            -   `protected final CustomItemStackHandler upgradeSlots` (e.g., size 3).
            -   `protected final CustomItemStackHandler outputSlots` (e.g., size 18).
            -   `protected final ContainerData data` (handling `progress` and `maxProgress`).
            -   `protected int progress = 0;`
            -   `protected int maxProgress = DEFAULT_MAX_PROGRESS;` (Define `DEFAULT_MAX_PROGRESS`).
            -   `protected List<ItemStack> outputItems = new ArrayList<>();`
            -   `protected final CombinedInvWrapper combinedInvWrapper` (Initialized in constructor using subclass-provided handlers, with logic to restrict input/output).
        -   **[x] Abstract Methods/Requirements:**
            -   `protected abstract RecipeType<? extends Recipe<T>> getRecipeType();` (Replace `T` with a suitable bound if possible, maybe just `RecipeInput`).
            -   `protected abstract RecipeInput createRecipeInput();`
            -   `protected abstract CustomItemStackHandler getInputSlotsHandler();`
            -   `protected abstract CustomItemStackHandler getExtraSlotsHandler();` (Consider if *all* farms need extra slots, might need adjustment).
            -   `@Override public abstract @NotNull Component getDisplayName();`
            -   `@Override public abstract @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInv, @NotNull Player player);`
        -   **[x] Common Methods:**
            -   Implement `tick(Level, BlockPos, BlockState)` containing the core processing loop (`hasRecipe`, `increaseCraftingProgress`, `hasCraftingFinished`, `craftItem`, `resetProgress`).
            -   Implement `hasRecipe()` using `getCurrentRecipe()` and `canInsertItemIntoOutputSlots()`.
            -   Implement `getCurrentRecipe()` using `getRecipeType()` and `createRecipeInput()`.
            -   Implement `canInsertItemIntoOutputSlots(List<ItemStack>)`.
            -   Implement `craftItem()` to get recipe, roll results (`ChanceResult.rollResults`), and insert into `outputSlots`. (Subclasses might override parts for tool damage/input consumption).
            -   Implement `increaseCraftingProgress()`, `hasCraftingFinished()`, `resetProgress()`.
            -   Implement `drops()` to drop contents of *all* handlers (input, extra, upgrade, output). Use `getInputSlotsHandler()`, `getExtraSlotsHandler()` etc.
            -   Implement `saveAdditional` / `loadAdditional` to handle common fields (`progress`, `maxProgress`) and delegate inventory saving/loading to handlers.
            -   Implement `getCapabilityHandler(Direction)` returning `combinedInvWrapper`.
    -   **[x] `AbstractFarmMenu.java`:**
        -   Extend `AbstractContainerMenu`.
        -   Add fields: `blockEntity`, `levelAccess`, `data`.
        -   Implement constructor taking `MenuType`, `containerId`, `playerInv`, `blockEntity`, `data`. Call `super`, assign fields, call `addDataSlots`.
        -   Implement `createPlayerInventory` and `createPlayerHotbar` methods.
        -   Implement `stillValid`.
        -   Implement `quickMoveStack` (ensure TE slot indices are flexible or correctly calculated based on subclass slot counts).
        -   Leave space/call abstract method for adding BE-specific slots in subclass constructors.
    -   **[X] `AbstractFarmScreen.java`:**
        -   Extend `AbstractContainerScreen<T extends AbstractFarmMenu>`.
        -   Implement constructor.
        -   Implement basic `render(GuiGraphics, int, int, float)` calling `super.render` and `renderTooltip`.
        -   Implement basic `renderBg` (potentially drawing a common background part).
        -   Subclasses will provide specific texture, dimensions, slot rendering coordinates.
-   **[x] Create Shared RecipeInput Records:**
    -   **[x] `ThreeSlotFarmInput.java`:**
        -   `public record ThreeSlotFarmInput(ItemStack tool, ItemStack primary, ItemStack secondary) implements RecipeInput`
        -   Implement `getItem(int)`, `size()` (returns 3), and potentially named accessors (`tool()`, `primary()`, `secondary()`).
    -   **[x] `TwoSlotFarmInput.java`:**
        -   `public record TwoSlotFarmInput(ItemStack tool, ItemStack primary) implements RecipeInput`
        -   Implement `getItem(int)`, `size()` (returns 2), and potentially named accessors (`tool()`, `primary()`).
-   **[ ] Modify Existing Crop Farm Classes:**
    -   **[x] `CropFarmBlock`:**
        -   Change `extends Block` to `extends AbstractFarmBlock`.
        -   Implement `newBlockEntity` returning `new CropFarmBlockEntity(...)`.
        -   Remove methods now present in `AbstractFarmBlock`.
    -   **[ ] `CropFarmBlockEntity`:**
        -   Change `extends BlockEntity` to `extends AbstractFarmBlockEntity`.
        -   **[ ] Fields:** Ensure `inputSlots` (size 3) and `extraSlots` (size 3) are still defined and initialized.
        -   **[ ] Constructor:** Update to initialize `combinedInvWrapper` correctly with all its handlers.
        -   **[ ] Implement Abstract Methods:**
            -   `getRecipeType()`: return `ModRegistry.CROP_FARM_TYPE.get()`.
            -   `createRecipeInput()`: return `new ThreeSlotFarmInput(this.inputSlots.getStackInSlot(TOOL_INPUT_SLOT), ...)`. (Define TOOL/INGREDIENT/BASE slot constants if not already done).
            -   `getInputSlotsHandler()`: return `this.inputSlots`.
            -   `getExtraSlotsHandler()`: return `this.extraSlots`.
            -   `getDisplayName()`: return the existing title.
            -   `createMenu()`: return `new CropFarmMenu(...)`.
        -   **[ ] Remove Duplicated Code:** Delete methods/fields now present in `AbstractFarmBlockEntity`.
        -   **[ ] Override `craftItem` (Optional but likely needed):** Add logic specific to Crop Farm if needed (e.g., damaging the hoe, consuming ingredients if necessary - current code doesn't seem to do this, decide if it should).
    -   **[ ] `CropFarmMenu`:**
        -   Change `extends AbstractContainerMenu` to `extends AbstractFarmMenu`.
        -   Update constructor signature if needed; ensure `super` call passes `ModRegistry.CROP_FARM_MENU.get()`.
        -   Keep/move the `createBlockEntityInventory` logic (adding input, extra, upgrade, output slots specific to Crop Farm layout) into the constructor *after* the `super` call.
        -   Remove methods now present in `AbstractFarmMenu`.
    -   **[ ] `CropFarmScreen`:**
        -   Change `extends AbstractContainerScreen` to `extends AbstractFarmScreen`.
        -   Update constructor.
        -   Keep specific `renderBg` logic (texture, dimensions, slot rendering).
        -   Remove methods now present in `AbstractFarmScreen` (if any).
    -   **[ ] `CropFarmRecipeInput.java`:** Delete this file (will use `ThreeSlotFarmInput`). Update `CropFarmRecipe.matches` and `CropFarmRecipe.Serializer` stream codec to use `ThreeSlotFarmInput`.
-   **[ ] Testing:**
    -   **[ ] Launch Game:** Ensure it loads without crashes.
    -   **[ ] Craft & Place:** Craft and place the Crop Farm Block.
    *   **[ ] GUI:** Open the GUI. Check layout.
    *   **[ ] Recipe Processing:** Test existing recipes (Carrot, Oak, Spruce). Do they process correctly? Are outputs generated?
    *   **[ ] Item Transfer:** Test Shift-clicking items between player inventory and farm inventory.
    *   **[ ] Block Breaking:** Break the block. Do items drop correctly?
    *   **[ ] Hopper Interaction:** Test inserting/extracting items via hoppers (respecting `CombinedInvWrapper` rules).

## Phase 2: Implement Tree Farm (`ThreeSlotFarmInput`)

-   **[ ] Registration (`ModRegistry.java`):**
    -   **[ ] Block:** Add `TREE_FARM_BLOCK`.
    -   **[ ] Item:** Register corresponding `BlockItem`.
    -   **[ ] Block Entity:** Add `TREE_FARM_BLOCK_ENTITY` (`BlockEntityType.Builder.of(TreeFarmBlockEntity::new, TREE_FARM_BLOCK.get())`).
    -   **[ ] Menu:** Add `TREE_FARM_MENU` (`IMenuTypeExtension.create(TreeFarmMenu::new)`).
    -   **[ ] Recipe Serializer:** Add `TREE_FARM_SERIALIZER` (`new TreeFarmRecipe.Serializer()`).
    -   **[ ] Recipe Type:** Add `TREE_FARM_TYPE` (`new RecipeType<TreeFarmRecipe>() { ... }`, name "tree_farm").
-   **[ ] Recipe Definition:**
    -   **[ ] `TreeFarmRecipe.java`:** Create record (likely same fields as `CropFarmRecipe`: tool, ingredient, base, outputs). Adapt `matches` if needed.
    -   **[ ] `TreeFarmRecipe.Serializer.java`:** Implement codec and stream codec (using `ThreeSlotFarmInput` for stream codec).
    -   **[ ] `TreeFarmRecipeBuilder.java`:** Create builder class for Tree Farm recipes.
    -   **[ ] `ModRecipeProvider.java`:** Add recipes using `TreeFarmRecipeBuilder`.
-   **[ ] Block Implementation:**
    -   **[ ] `TreeFarmBlock.java`:** Create class extending `AbstractFarmBlock`. Implement `newBlockEntity`.
    -   **[ ] `TreeFarmBlockEntity.java`:** Create class extending `AbstractFarmBlockEntity`.
        -   Define `inputSlots` (size 3) and `extraSlots` (size 3).
        -   Implement abstract methods (`getRecipeType` -> `TREE_FARM_TYPE`, `createRecipeInput` -> `new ThreeSlotFarmInput(...)`, etc.).
        -   Define `getDisplayName`.
        -   Implement `createMenu` -> `new TreeFarmMenu(...)`.
        -   Override `craftItem` if tool damage (axe) or ingredient consumption logic is needed.
-   **[ ] GUI/Menu Implementation:**
    -   **[ ] `TreeFarmMenu.java`:** Create class extending `AbstractFarmMenu`. Implement BE slot layout in constructor.
    -   **[ ] `TreeFarmScreen.java`:** Create class extending `AbstractFarmScreen`. Define texture, dimensions, render slots.
-   **[ ] Data Generation:**
    -   **[ ] `ModRecipeProvider`:** Ensure recipes are saved.
    -   **[ ] `ModBlockStateProvider`:** Add state and model for `TREE_FARM_BLOCK`.
    -   **[ ] `ModItemModelProvider`:** Add item model.
    -   **[ ] `ModBlockLootTableProvider`:** Add loot table (e.g., `dropSelf`).
    -   **[ ] `ModBlockTagProvider`:** Add relevant tags (e.g., `mineable_with_axe`?).
-   **[ ] Assets:**
    -   **[ ] Textures:** Add block texture(s) (`assets/betterfarms/textures/block/tree_farm_block.png`...). Add GUI texture (`assets/betterfarms/textures/gui/tree_farm_screen.png`).
    -   **[ ] Language:** Add `block.betterfarms.tree_farm_block` to `en_us.json`.
-   **[ ] Testing:** Test crafting, placement, GUI, recipe processing, drops, etc., specifically for the Tree Farm.

## Phase 3: Implement Stone Farm (`TwoSlotFarmInput`)

-   **[ ] Registration (`ModRegistry.java`):** Register `STONE_FARM_BLOCK`, item, `STONE_FARM_BLOCK_ENTITY`, `STONE_FARM_MENU`, `STONE_FARM_SERIALIZER`, `STONE_FARM_TYPE`.
-   **[ ] Recipe Definition:**
    -   **[ ] `StoneFarmRecipe.java`:** Create record (likely only `tool`, `primary`, `outputs`).
    -   **[ ] `StoneFarmRecipe.Serializer.java`:** Implement codec/stream codec (using `TwoSlotFarmInput`). Handle potentially fewer fields in JSON.
    -   **[ ] `StoneFarmRecipeBuilder.java`:** Create builder.
    -   **[ ] `ModRecipeProvider.java`:** Add recipes.
-   **[ ] Block Implementation:**
    -   **[ ] `StoneFarmBlock.java`:** Create class extending `AbstractFarmBlock`.
    -   **[ ] `StoneFarmBlockEntity.java`:** Create class extending `AbstractFarmBlockEntity`.
        -   Define `inputSlots` (size **2**). Decide if `extraSlots` are needed (if not, `getExtraSlotsHandler` could return an empty handler or be removed from abstract if not universal).
        -   Implement abstract methods (`getRecipeType` -> `STONE_FARM_TYPE`, `createRecipeInput` -> `new TwoSlotFarmInput(...)`, `getInputSlotsHandler` -> returns 2-slot handler, handle `getExtraSlotsHandler`).
        -   Define `getDisplayName`.
        -   Implement `createMenu` -> `new StoneFarmMenu(...)`.
        -   Override `craftItem` if tool damage (pickaxe) or input consumption (e.g., water bucket) is needed.
-   **[ ] GUI/Menu Implementation:**
    -   **[ ] `StoneFarmMenu.java`:** Create class extending `AbstractFarmMenu`. Implement BE slot layout (**2** input slots).
    -   **[ ] `StoneFarmScreen.java`:** Create class extending `AbstractFarmScreen`. Define texture, dimensions, render slots for the 2-input layout.
-   **[ ] Data Generation:** Add states, models, loot, tags for Stone Farm.
-   **[ ] Assets:** Add textures (block, GUI) and lang entry.
-   **[ ] Testing:** Test the Stone Farm thoroughly.

## Phase 4: Implement Ore Farm (`TwoSlotFarmInput`)

-   **[ ] Decide Ore Input:** Determine the `primary` input ingredient concept for ore generation.
-   **[ ] Registration:** Register `ORE_FARM_*` components.
-   **[ ] Recipe Definition:** Create `OreFarmRecipe`, `Serializer`, `Builder`. Define recipes in `ModRecipeProvider`.
-   **[ ] Block Implementation:** Create `OreFarmBlock`, `OreFarmBlockEntity` (using 2 input slots, `TwoSlotFarmInput`). Implement abstract methods. Override `craftItem` for pickaxe damage/input consumption.
-   **[ ] GUI/Menu Implementation:** Create `OreFarmMenu`, `OreFarmScreen` (2-input layout).
-   **[ ] Data Generation:** Add states, models, loot, tags.
-   **[ ] Assets:** Add textures, lang entry.
-   **[ ] Testing:** Test the Ore Farm.

## Phase 5: Implement Mob Farm (`TwoSlotFarmInput`)

-   **[ ] Decide Mob Input:** Determine how the "Captured Mob" input works (e.g., a specific item representing a mob type, potentially using NBT?).
-   **[ ] Registration:** Register `MOB_FARM_*` components.
-   **[ ] Recipe Definition:** Create `MobFarmRecipe`, `Serializer`, `Builder`. Define recipes. Consider how recipes will handle different "captured mobs".
-   **[ ] Block Implementation:** Create `MobFarmBlock`, `MobFarmBlockEntity` (using 2 input slots, `TwoSlotFarmInput`). Implement abstract methods. Handle the specific "captured mob" input logic. Override `craftItem` for sword damage/input consumption.
-   **[ ] GUI/Menu Implementation:** Create `MobFarmMenu`, `MobFarmScreen` (2-input layout).
-   **[ ] Data Generation:** Add states, models, loot, tags.
-   **[ ] Assets:** Add textures, lang entry.
-   **[ ] Testing:** Test the Mob Farm.

## Phase 6: Final Review & Polish

-   **[ ] Code Review:** Look for any remaining duplicated code, inconsistent naming, or areas for improvement.
-   **[ ] Configuration:** Consider adding server config options (e.g., default processing times, output multipliers).
-   **[ ] JEI/REI Integration (Optional but Recommended):** Add compatibility to show your custom farm recipes.
-   **[ ] Update `README.md`:** Document the new blocks and their usage.
-   **[ ] Test Everything Together:** Ensure all farms work correctly in the same environment.