package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.IGridService;
import appeng.api.stacks.AEItemKey;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;

/**
 * Provides EMC-related functionality to an AE2 network when at least one Transmutation Module is installed on it.
 *
 * @apiNote For item transmutation, insertion/extraction operations are carried out through regular means via AE2's
 * {@link appeng.api.networking.storage.IStorageService IStorageService}, but must be done with a machine that
 * implements {@link gripe._90.appliede.me.misc.TransmutationCapable TransmutationCapable} as the "action source" for
 * these operations to be able to transmute items into / out of EMC.
 */
public interface KnowledgeService extends IGridService {
    /**
     * @param uuid The UUID for a given player's Minecraft profile
     * @return The {@link IKnowledgeProvider} for the (tracked) player with the given UUID (typically via player
     * {@linkplain net.neoforged.neoforge.capabilities.EntityCapability entity capabilities}), or {@code null} if the
     * UUID is not being tracked (i.e. the player with the given UUID does not have a Transmutation Module installed
     * onto the network).
     */
    @Nullable
    IKnowledgeProvider getProviderFor(@Nullable UUID uuid);

    /**
     * @return The total EMC amount of all players tracked by Transmutation Modules on this network.
     * @implNote This value is cached and updated at most once per tick after any EMC I/O operations on the network.
     */
    BigInteger getEMC();

    /**
     * @return The items currently "learned" by all players tracked by Transmutation Modules on this network.
     */
    Set<AEItemKey> getKnownItems();
}
