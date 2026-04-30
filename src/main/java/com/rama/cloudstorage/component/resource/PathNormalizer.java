package com.rama.cloudstorage.component.resource;

import com.rama.cloudstorage.exception.InvalidOperationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class PathNormalizer {

    public List<String> normalizeToSegments(String path) {
        if (!StringUtils.hasText(path)) {
            return List.of();
        }

        String standardizedPath = path.replace("\\", "/");
        String[] parts = standardizedPath.split("/");

        Deque<String> stack = new ArrayDeque<>();

        for (String part : parts) {
            part = part.trim();

            if (part.isEmpty() || part.equals(".")) {
                continue;
            }

            if (part.equals("..")) {
                if (!stack.isEmpty()) {
                    stack.removeLast();
                } else {
                    throw new InvalidOperationException("Attempting to go above the boot root: " + path);
                }
            } else {
                stack.addLast(part);
            }
        }

        return new ArrayList<>(stack);
    }

    public String builderPath(List<String> segments) {
        return String.join("/", segments);
    }

    public String extractDirectoryPath(String fullPath) {
        List<String> segments = normalizeToSegments(fullPath);
        if (segments.size() <= 1) {
            return "";
        }

        return builderPath(segments.subList(0, segments.size() - 1));
    }
}
