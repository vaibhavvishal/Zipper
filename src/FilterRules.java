import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FilterRules {

    private final List<Rule> rules;
    private final List<Rule> rulesFile;
    private final List<Rule> rulesDir;
    private final List<Rule> rulesMix;

    FilterRules() {
        rules = new LinkedList<>();
        rulesDir = new LinkedList<>();
        rulesFile = new LinkedList<>();
        rulesMix = new LinkedList<>();
    }

    public void addRule(Rule rule) {
        rules.add(rule);
        switch (rule.mType) {
        case FILE:
        case REGEX_FILE:
            rulesFile.add(rule);
            break;
        case DIR:
        case REGEX_DIR:
            rulesDir.add(rule);
            break;
        case REGEX_FILE_DIR:
            rulesMix.add(rule);
            break;
        }
    }

    public void clear() {
        if (rules != null) {
            rules.clear();
        }
    }

    public boolean check(String file, Rule.Type type) {
        boolean ret = false;

        switch (type) {
        case FILE:
            for (Rule rule : rulesFile) {
                String str = rule.rule;
                if (match(file, str, true)) {
                    ret = true;
                    break;
                }
            }
            return ret;
        case DIR:
            for (Rule rule : rulesDir) {
                String str = rule.rule;
                int lastSeperator = str.lastIndexOf(File.separator);
                int firstSeperator = str.indexOf(File.separator);
                if (lastSeperator > 0 && firstSeperator > 0) {
                    if (lastSeperator == firstSeperator) {
                        str = str.replace(File.separator, "");
                    } else {
                        str = str.substring(firstSeperator, lastSeperator);
                    }
                } else if (firstSeperator > 0) {
                    str = str.substring(firstSeperator);
                } else if (lastSeperator > 0) {
                    str = str.substring(0, lastSeperator);
                }

                if (match(file, str, false)) {
                    ret = true;
                    break;
                }
            }
            break;
        default:
            return false;
        }

        return ret;
    }

    private boolean match(String file, String rule, boolean isFile) {
        if (file.equalsIgnoreCase(rule)) {
            return true;
        }

        if (isFile) {
            String[] fileSplit = file.split("\\.");
            String[] ruleSplit = rule.split("\\.");

            if (ruleSplit[0].equals("*") && ruleSplit[1].equals("*")) {
                return true;
            } else if (ruleSplit[0].equals("*")) {
                if (fileSplit[1].equals(ruleSplit[1])) {
                    return true;
                } else {
                    return false;
                }
            } else if (ruleSplit[1].equals("*")) {
                if (fileSplit[0].equals(ruleSplit[0])) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {

        }

        return false;
    }
}

class Rule {
    public String rule;

    public enum Type {
        FILE,
        REGEX_FILE,
        DIR,
        REGEX_DIR,
        REGEX_FILE_DIR
    }

    public Type mType;

    Rule() {
    }

    Rule(String rule) {
        this.rule = rule;
    }
}
