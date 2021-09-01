package duke;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import duke.exception.DukeException;
import duke.task.Deadline;
import duke.task.Event;
import duke.task.Task;
import duke.task.TaskList;
import duke.task.ToDo;

/**
 * Allows users to add 3 different types of tasks, mark them as done, and delete tasks.
 *
 * The commands for usage are as follows:
 * 1. "todo name" where name is what the user would like the todo to be called.
 * 2. "event name /at YYYY-MM-DD"
 * 3. "deadline name /by YYYY-MM-DD"
 * 4. "list" to view current tasks added to the tasks list.
 * 5. "done taskNumber" to mark task as completed.
 * 6. "delete taskNumber" to delete task.
 * 7. "bye" to leave the ChatBot.
 * Disclaimer: any other commands will not be recognised and user will be prompted to enter a valid command.
 *
 * @author Leong Hong Fai
 */

public class Duke {
    private static final String fileAddress = "data/duke.txt";

    private static Storage storage;
    private static TaskList tasks;
    private Ui ui;

    /**
     * Creates a Duke object.
     */
    public Duke() {
        this.ui = new Ui();
        this.storage = new Storage(fileAddress);
        try {
            this.tasks = new TaskList(storage.load());
        } catch (DukeException e) {
            //ui.showLoadingError();
            this.tasks = new TaskList();
        }
    }

    /**
     * You should have your own function to generate a response to user input.
     * Replace this stub with your completed method.
     */
    String[] getResponse(String input) {
        return commands(input);
    }

    /*
    public void run() {
        commands();
    }
     */


    /**
     * Adds a ToDo to the list of tasks.
     *
     * @param command Command entered by the user.
     * @throws DukeException Upon invalid command format.
     */
    public String[] addToDo(String command, boolean printOutput) throws DukeException {
        if (command.length() < 6 || command == null) {
            throw new DukeException("invalidToDo");
        } else {
            String name = command.substring(5);
            Task task = new ToDo(name);
            tasks.add(task);
            if (printOutput) {
                try {
                    storage.appendToFile(fileAddress, "T - 0 - " + name);
                } catch (IOException e) {
                    return new String[] {e.toString()};
                }
                return ui.printTaskAdded(task, tasks.size());
            }
        }
        return new String[0];
    }

    /**
     * Adds a deadline to the list of tasks.
     *
     * @param command Command entered by the user.
     * @throws DukeException Upon invalid command format.
     */
    public String[] addDeadline(String command, boolean printOutput) throws DukeException {
        String[] words = command.split(" ");
        if (words.length <= 3) {
            throw new DukeException("invalidDeadline");
        } else if (!command.contains("/by")) {
            throw new DukeException("invalidDeadline");
        } else {
            try {
                LocalDate.parse(words[3]);
                int position = command.indexOf("/by");
                String name = command.substring(9, position);
                String date = command.substring(position + 4);
                Task task = new Deadline(name, date);
                tasks.add(task);
                if (printOutput) {
                    storage.appendToFile(fileAddress, "D - 0 - " + name + "- " + date);
                    return ui.printTaskAdded(task, tasks.size());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DateTimeParseException e) {
                return new String[] {("Enter valid date format!")};
            }
        }
        return new String[0];
    }

    /**
     * Adds event to the list of tasks.
     *
     * @param command Command entered by the user.
     * @throws DukeException Upon invalid command format.
     */
    public String[] addEvent(String command, boolean printOutput) throws DukeException {
        String[] words = command.split(" ");
        if (words.length <= 3) {
            throw new DukeException("invalidEvent");
        } else if (!command.contains("/at")) {
            throw new DukeException("invalidEvent");
        } else {
            try {
                LocalDate.parse(words[3]);
                int position = command.indexOf("/at");
                String name = command.substring(6, position);
                String date = command.substring(position + 4);
                Task task = new Event(name, date);
                tasks.add(task);
                if (printOutput) {
                    storage.appendToFile(fileAddress, "E - 0 - " + name + "- " + date);
                    return ui.printTaskAdded(task, tasks.size());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DateTimeParseException e) {
                return new String[]{("Enter valid date format!")};
            }
        }
        return new String[0];
    }

    /**
     * Marks a specific task in the list as completed.
     *
     * @param command Command entered by the user.
     * @throws DukeException Upon incorrect command format.
     */
    public String[] markCompleted(String command, boolean printOutput) throws DukeException {
        boolean numeric;
        String restOfCommand = "";
        try {
            restOfCommand = command.substring(5);
            int temp = Integer.parseInt(restOfCommand);
            numeric = true;
        } catch (NumberFormatException err) {
            numeric = false;
        } catch (StringIndexOutOfBoundsException err) {
            throw new DukeException("invalidNumberFormat");
        }
        if (numeric) {
            int taskNum = Integer.parseInt(restOfCommand) - 1;
            if (taskNum < tasks.size()) {
                Task currTask = tasks.get(taskNum);
                currTask.setCompleted();
                if (printOutput) {
                    storage.editFileContentsForCompletion(taskNum + 1);
                    return ui.printTaskCompleted(currTask);
                }
            } else {
                throw new DukeException("invalidTaskNumber");
            }
        } else {
            throw new DukeException("invalidNumberFormat");
        }
        return new String[0];
    }

    /**
     * Deletes a specified task from the list of tasks.
     *
     * @param command Command entered by the user.
     * @throws DukeException Upon incorrect command format.
     * @return
     */
    private String[] deleteTask(String command) throws DukeException {
        String restOfCommand = command.substring(7);
        boolean numeric;
        try {
            int temp = Integer.parseInt(restOfCommand);
            numeric = true;
        } catch (NumberFormatException err) {
            numeric = false;
        }
        if (numeric) {
            int taskNum = Integer.parseInt(restOfCommand) - 1;
            if (taskNum < tasks.size()) {
                storage.editFileContentsForDeletion(taskNum + 1);
                Task currTask = tasks.get(taskNum);
                tasks.remove(taskNum);
                return ui.printDeleteTask(currTask, tasks.size());
            } else {
                throw new DukeException("invalidTaskNumber");
            }
        } else {
            throw new DukeException("invalidNumberFormatDelete");
        }
    }

    /**
     * Facilitates which command to run based on the type of command processed by the Parser.
     *
     * @throws DukeException If command is not recognised or improperly formatted.
     */
    private String[] commands(String command) throws DukeException {
        String parsed = Parser.process(command);
        try {
            switch (parsed) {
            case ("bye"):
                return ui.end();
            case ("list"):
                return tasks.printList(command);
            case ("todo"):
                return addToDo(command, true);
            case ("deadline"):
                return addDeadline(command, true);
            case ("event"):
                return addEvent(command, true);
            case ("done"):
                return markCompleted(command, true);
            case ("delete"):
                return deleteTask(command);
            case ("find"):
                return findTasks(command);
            default:
                return new String[] {"invalid command"};
            }
        } catch (DukeException err) {
            return new String[] {err.toString()};
        }
    }

    private String[] findTasks(String command) {
        Task[] result = new Task[tasks.size()];
        String[] words = command.split(" ");
        int count = 0;
        if (words.length == 1) {
            throw new DukeException("invalidFindTask");
        } else {
            String piece = command.substring(5);
            for (int i = 0; i < tasks.size(); i++) {
                String name = tasks.get(i).getName();
                if (name.contains(piece)) {
                    result[count] = tasks.get(i);
                    count++;
                }
            }
        }
        return ui.printFindTask(result);
    }
}





