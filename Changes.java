Map<String, List<Path>> inputIndex = null;   // CHANGE (CIF-ONLY)
try (ZipFile zipFile = new ZipFile(tempZip.toFile(), password.toCharArray())) {






// ===== CHANGE (CIF-ONLY) =====
Path sourceFolder = customerFolder;
if(!(Files.exists(customerFolder)&&Files.isDirectory(customerFolder))) {
    if (inputIndex == null) inputIndex = indexInputFoldersByCif(customerFolder.getParent());
    sourceFolder = pickFolderForCif(inputIndex, customerId);
}
if (sourceFolder == null) {
    logger.info("No folder found in the LCPC folder for CIF of " + customerId);
    markZipFailed(customerId, "Zip not created - folder missing in input");
    continue;
}
Path claimed = claimFolder(sourceFolder, processingDir);
// ===== END CHANGE (CIF-ONLY) =====









// ===== CHANGE (CIF-ONLY): new helper methods =====
private static String cifKey(String s) {
    return s == null ? "" : s.trim().replaceFirst("^0+(?!$)", "");
}

private Map<String, List<Path>> indexInputFoldersByCif(Path inputDir) {
    Map<String, List<Path>> index = new HashMap<>();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(inputDir, p -> Files.isDirectory(p))) {
        for (Path p : ds) {
            String name = p.getFileName().toString();
            int i = name.indexOf('_');
            index.computeIfAbsent(cifKey(i > 0 ? name.substring(0, i) : name), k -> new ArrayList<>()).add(p);
        }
    } catch (IOException e) {
        logger.error("Could not read the LCPC input folder " + inputDir, e);
    }
    return index;
}

private Path pickFolderForCif(Map<String, List<Path>> index, String customerId) {
    String[] v = customerId.split("_");
    List<Path> found = index.get(cifKey(v[0]));
    if (found == null || found.isEmpty()) return null;
    if (found.size() == 1) return found.get(0);
    if (v.length > 2) {
        for (Path p : found) {
            String[] f = p.getFileName().toString().split("_");
            if (f.length > 2 && cifKey(f[2]).equals(cifKey(v[2]))) return p;
        }
    }
    logger.error("More than one folder found for CIF {} : {}", v[0], found);
    return null;
}
// ===== END CHANGE (CIF-ONLY) =====
