package eu.reportincident.userservice.service;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    Map<String, Map<String, List<String>>> getPermissionMap();
}
