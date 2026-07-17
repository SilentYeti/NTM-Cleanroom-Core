package com.ntmcleanroom.compat.tinkers.traits;

import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared "did I just break this myself" tracker for {@link AoeTrait}/{@link FlatAoeTrait}/
 * {@link VeinMinerTrait}. A same-tick dedup alone isn't enough to stop these from spreading far
 * past their configured radius: if a block we destroy via {@code world.destroyBlock} inside our
 * own expansion loop reports back through {@code afterBlockBreak} on a *later* tick rather than
 * immediately, each generation of newly-broken blocks would spawn its own fresh expansion,
 * compounding into a runaway cavern instead of the intended one-shot area effect.
 *
 * <p>Every position an expansion is about to destroy is recorded here first; any
 * {@code afterBlockBreak} call for a tracked position is recognized as our own echo (regardless of
 * which tick it lands on) and consumed without triggering a new expansion. Entries expire after a
 * few seconds so a stray, never-echoed entry can't leak forever.
 */
final class AreaAbilityGuard {

    private static final long EXPIRY_MILLIS = 5000L;
    private static final Map<BlockPos, Long> selfDestroyed = new ConcurrentHashMap<>();

    private AreaAbilityGuard() {}

    /** Call right before destroying a block as part of an expansion, so its own echo is recognized and ignored. */
    static void markSelfDestroyed(BlockPos pos) {
        if (selfDestroyed.size() >= 10_000) {
            purgeStale();
        }
        selfDestroyed.put(pos.toImmutable(), System.currentTimeMillis());
    }

    /** True if this position was just destroyed by one of our own expansions - consumes the entry. */
    static boolean isEcho(BlockPos pos) {
        Long timestamp = selfDestroyed.remove(pos);
        return timestamp != null && System.currentTimeMillis() - timestamp < EXPIRY_MILLIS;
    }

    private static void purgeStale() {
        long now = System.currentTimeMillis();
        selfDestroyed.entrySet().removeIf(entry -> now - entry.getValue() > EXPIRY_MILLIS);
    }
}
