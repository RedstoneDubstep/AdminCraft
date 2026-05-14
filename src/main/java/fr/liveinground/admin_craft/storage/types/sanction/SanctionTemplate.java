package fr.liveinground.admin_craft.storage.types.sanction;

import javax.annotation.Nullable;

public record SanctionTemplate(String name, String sanctionMessage, Sanction type, @Nullable String duration) { }
