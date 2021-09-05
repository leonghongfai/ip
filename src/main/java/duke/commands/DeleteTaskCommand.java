package duke.commands;

import duke.storage.Storage;
import duke.ui.Ui;
import duke.exception.DukeException;
import duke.task.Task;
import duke.task.TaskList;

/**
 * Creates an AddDeadlineCommand to add deadlines to the task list.
 */
public class DeleteTaskCommand extends Command {
    private String command;

    public DeleteTaskCommand(String command) {
        assert command.startsWith("delete");
        this.command = command;
    }

    /**
     * Executes the Command accordingly.
     *
     * @param storage Storage to store changes in text file.
     * @param tasks Tasks compiled in a TaskList.
     * @return A String array containing output.
     */
    public String[] execute(Storage storage, TaskList tasks) {
        String restOfCommand = "";
        boolean numeric;
        try {
            restOfCommand = command.substring(7);
            int temp = Integer.parseInt(restOfCommand);
            numeric = true;
        } catch (NumberFormatException err) {
            numeric = false;
        } catch (StringIndexOutOfBoundsException err) {
            throw new DukeException("invalidNumberFormat");
        }
        if (!numeric) {
            throw new DukeException("invalidNumberFormatDelete");
        }
        int taskNum = Integer.parseInt(restOfCommand) - 1;
        if (!(taskNum < tasks.size())) {
            throw new DukeException("invalidTaskNumber");
        }
        storage.editFileContentsForDeletion(taskNum + 1);
        Task currTask = tasks.get(taskNum);
        tasks.remove(taskNum);
        return Ui.printDeleteTask(currTask, tasks.size());
    }
}