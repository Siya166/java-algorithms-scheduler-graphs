package dev.tatenda.algorithms.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Rebuilds a path by walking a parent map backwards from the goal to the start. */
final class PathBuilder {

    private PathBuilder() {
    }

    static <T> List<T> build(Map<T, T> parent, T start, T goal) {
        List<T> path = new ArrayList<>();
        T at = goal;
        path.add(at);
        while (!at.equals(start)) {
            at = parent.get(at);
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}
