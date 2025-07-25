package eu.reportincident.gatewayservice.dto;

import java.util.List;

// Ovaj DTO mora da odgovara tipu koji vraća PermissionController
// java.util.HashMap se može serijalizovati/deserijalizovati bez problema
public class PermissionMapDto extends java.util.HashMap<String, List<String>> {
}