package com.rama.cloudstorage.service.resource.tree;

import com.rama.cloudstorage.component.resource.PathNormalizer;
import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.entity.User;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectoryTreeResolver {

    private final ResourceRepository resourceRepository;
    private final PathNormalizer pathNormalizer;

    public Map<String, Resource> resolveAndCreateTree(User user, Resource rootParent, Set<String> relativePath) {
        Map<Integer, Set<List<String>>> pathsBeDepth = new HashMap<>();
        int maxDepth = 0;

        for (String path : relativePath) {
            List<String> segments = pathNormalizer.normalizeToSegments(path);
            for (int i = 1; i <= segments.size(); i++) {
                List<String> subPath = segments.subList(0, i);
                pathsBeDepth.computeIfAbsent(i, k -> new HashSet<>()).add(subPath);
                maxDepth = Math.max(maxDepth, i);
            }
        }

        Map<String, Resource> pathCache = new HashMap<>();
        pathCache.put("", rootParent);

        for (int depth = 1; depth <= maxDepth; depth++) {
            Set<List<String>> currentDepthPath = pathsBeDepth.get(depth);

            Set<String> targetNames = new HashSet<>();
            Set<UUID> parentIds = new HashSet<>();

            for (List<String> pathSegments : currentDepthPath) {
                targetNames.add(pathSegments.get(depth - 1));

                String parentPath = pathNormalizer.builderPath(pathSegments.subList(0, depth - 1));
                Resource parentResource = pathCache.get(parentPath);
                if (parentResource != null) {
                    parentIds.add(parentResource.getId());
                }
            }

            List<Resource> existingDirectories = fetchExistingDirectories(user.getId(), rootParent, depth, parentIds, targetNames);

            for (Resource directory : existingDirectories) {
                String pathKey = buildPathKey(pathCache, directory);
                pathCache.put(pathKey, directory);
            }

            List<Resource> missingDirectoriesToSave = new ArrayList<>();
            for (List<String> pathSegments : currentDepthPath) {
                String fullPath = pathNormalizer.builderPath(pathSegments);

                if (!pathCache.containsKey(fullPath)) {
                    String parentPath = pathNormalizer.builderPath(pathSegments.subList(0, depth - 1));
                    Resource parent = pathCache.get(parentPath);
                    String directoryName = pathSegments.get(depth - 1);

                    Resource newDirectory = Resource.createDirectory(user, parent, directoryName);
                    missingDirectoriesToSave.add(newDirectory);

                    pathCache.put(fullPath, newDirectory);
                }
            }

            if (!missingDirectoriesToSave.isEmpty()) {
                resourceRepository.saveAllAndFlush(missingDirectoriesToSave);
            }
        }
        return pathCache;
    }

    private List<Resource> fetchExistingDirectories(UUID userId, Resource rootParent, int depth, Set<UUID> parentIds, Set<String> names) {
        if (depth == 1 && rootParent == null) {
            return resourceRepository.findRootDirectoriesByNames(userId, names);
        } else {
            return resourceRepository.findDirectoriesByParentsAndNames(userId, parentIds, names);
        }
    }

    private String buildPathKey(Map<String, Resource> pathCache, Resource directory) {
        UUID parentId = directory.getParent() != null ? directory.getParent().getId() : null;

        String parentPath = pathCache.entrySet().stream()
                .filter(e -> {
                    UUID cachedId = e.getValue() != null ? e.getValue().getId() : null;
                    return Objects.equals(cachedId, parentId);
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");

        return parentPath.isEmpty() ? directory.getName() : parentPath + "/" + directory.getName();
    }
}
