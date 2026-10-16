package com.ftbqbridge.backend.ftbq;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Pure, Minecraft-free helpers for walking and naming quest objects, kept separate from
 * {@code FtbQuestsBackend} so they can be unit-tested without bootstrapping the game.
 */
public final class QuestTree {
    private QuestTree() {}

    /**
     * Every node of the tree rooted at {@code root}, each node's descendants before the node itself
     * (post-order), ending with {@code root}.
     *
     * <p>This is the id order FTB's own GUI delete uses ({@code DeleteObjectMessage.expandChildren}):
     * {@code ServerQuestFile.deleteObjects} only removes on-disk files and translations for the ids it
     * is given, so a container's descendants must be listed explicitly — otherwise deleting a chapter
     * group would leave its chapters' {@code .snbt} files behind to be reloaded on the next restart.
     */
    public static <T> List<T> childrenFirst(T root, Function<? super T, ? extends Collection<? extends T>> children) {
        List<T> out = new ArrayList<>();
        addChildrenFirst(root, children, out);
        return out;
    }

    private static <T> void addChildrenFirst(T node, Function<? super T, ? extends Collection<? extends T>> children, List<T> out) {
        for (T child : List.copyOf(children.apply(node))) addChildrenFirst(child, children, out);
        out.add(node);
    }

    /** The bare chapter filename from FTB's {@code Chapter.getPath()}: {@code chapters/foo.snbt} -> {@code foo}. */
    public static String chapterFilename(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".snbt") ? name.substring(0, name.length() - ".snbt".length()) : name;
    }
}
