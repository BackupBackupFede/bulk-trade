# Bulk Villager Trading — NeoForge + Fabric, MC 1.21.1 + 26.2 + 26.3

Trade your whole stock with a merchant in a single shift-click.

## What it does (and doesn't)

This one's counter-intuitive, so worth stating: **vanilla already chains trades** when you
shift-click a merchant's result slot (`AbstractContainerMenu.doClick` re-runs
`quickMoveStack` in a loop). But it only consumes what you **manually placed in the 2
payment slots** (~2 stacks), and **never refills** those slots from the rest of your
inventory.

This mod adds **exactly one thing**: it refills the payment slots from your inventory
before each trade. Result: one shift-click trades your *whole* relevant stock instead of
stopping after 2 stacks. It's a quality-of-life refinement (trading halls, converting
resources to emeralds), not a missing feature.

**It respects villager restock limits** — when a trade runs out of stock, bulk trading
stops, exactly like vanilla. No stock bypass, no duping.

Implementation: a single Mixin (`@Inject` at the HEAD of `quickMoveStack`, result slot)
that calls the vanilla `moveFromInventoryToPaymentSlot`, then lets vanilla do the trade.
No duplicated trade logic. Server-side, no dependencies.

| Config (server) | Default | Effect |
|---|---|---|
| `enabled` | `true` | Toggles the refill. `false` = strict vanilla behavior. |

## Build & run

Requires **JDK 21**. Gradle wrapper included.

```bash
cd bulk-trade
./gradlew build          # -> build/libs/bulktrade-0.1.0.jar
./gradlew runClient      # test client with the mod loaded
```

The first `build` decompiles Minecraft (slow, then cached); later builds take seconds.
In IntelliJ: "Open" the `bulk-trade` folder, then run the `runClient` run configuration.

## In-game test

1. Creative world, a villager with a profession (or a wandering trader).
2. Select a trade and place payment as usual (clicking the trade auto-fills the slots).
3. **Shift-click** the result slot.
4. Expected: the trade repeats until your payment stock is drained (not just the 2 slots),
   and stops when the villager runs out of stock. Compare `enabled=false` (stops after
   ~2 stacks) vs `true`.

Leftover payment in the slots is returned to your inventory when the menu closes
(`MerchantMenu.removed`), so no items are lost.

## Loader & versions

- **Loaders:** NeoForge and Fabric (no Fabric API needed)
- **Minecraft:** 1.21.1, 26.2 and 26.3 — one jar per loader and per version

## Support

☕ If this mod is useful to you: [buy me a coffee on Ko-fi](https://ko-fi.com/acesoverdeuces777)

## License

[MIT](LICENSE)
