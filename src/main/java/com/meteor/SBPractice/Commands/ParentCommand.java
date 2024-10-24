package com.meteor.SBPractice.Commands;

import java.util.List;

public interface ParentCommand {
    void addSubCommand(SubCommand subCommand);

    List<SubCommand> getSubCommands();
}
