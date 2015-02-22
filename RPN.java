import java.util.Scanner;
import java.util.Stack;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import javax.sound.midi.*;


/**
 * A public class that represents an RPN calculator.
 */
public class RPN {
	public static void main(String args[]) {
		System.out.println("Welcome to the CS9G RPN simulator!\n" +
						   "You can type numbers or operators (+, -, /, *, pop, clear, midi, quit)");
		Stack stack = new Stack();
		Scanner input = new Scanner(System.in);
		do {
			System.out.println("+- STACK -- top ---------+");
			for (int i = stack.size() - 1; i >= 0; --i) {
				Object elm = stack.get(i);
				System.out.println("|                      " + elm + " |");
			}
			System.out.print("+--------- bottom -------+ Input: ");

			try {
				String word = input.next();
				if (word.equals("quit")) {
					break;
				} else if (word.equals("clear")) {
					stack = new Stack();
				} else if (word.equals("pop")) {
					stack.pop();
				} else if (word.equals("midi")) {
					parseMIDI(stack);
				} else {
					if (word.equals("+")) {
						parseAddition(stack);
					} else if (word.equals("-")) {
						parseSubtraction(stack);
					} else if (word.equals("/")) {
						parseDivision(stack);
					} else if (word.equals("*")) {
						parseMultiplication(stack);
					} else if (isNumeric(word)) {
						stack.push(Integer.parseInt(word));
					} else {
						throw new IllegalArgumentException("Error! \"" + word + "\" is not a number or operator (+, -, /, *, pop, clear, midi, quit");
					}
				}
			} catch (EmptyStackException e) {
				System.err.println("Error! No more elements to pop.");
			} catch (IllegalArgumentException e) {
				System.err.println(e.getMessage());
			} catch (ArgumentNumberException e) {
				System.err.println(e.getMessage());
			} catch (NoSuchElementException e) {
				break;
			} catch (ZeroDivisionException e) {
				System.err.println(e.getMessage());
			} catch (InvalidTypeException e) {
				System.err.println(e.getMessage());
			} catch (DataOutOfRangeException e) {
				System.err.println(e.getMessage());
			} catch (InvalidMidiDataException e) {
				System.err.println(e.getMessage());
			} catch (MidiUnavailableException e) {
				System.err.println(e.getMessage());
			}
			//break;
		} while(true);
	}
	/**
	 * Helper function for checking if the str has all digits.
	 * @param  str input string
	 * @return     true iff string contains all digits
	 */
	private static boolean isNumeric(String str) {
	    for (char c : str.toCharArray()) {
	        if (!Character.isDigit(c)) {
	        	throw new IllegalArgumentException("Error! \"" + str + "\" is not a number or operator (+, -, /, *, pop, clear, midi, quit");
	    	}
	    }
	    return true;
	}
	/**
	 * Parses the division operation by applying '/' to the next two elements on the stack.
	 * @param  s                           The pointer to the Stack to which the divison opeartion will be applied.
	 * @throws ZeroDivisionException       Exception for divison by zero.
	 * @throws ArgumentNumberException Exception for invalid number of arguments.
	 * @throws  InvalidTypeException 	Exception for invalid types of arguments.
	 */
	private static void parseDivision(Stack s) throws ZeroDivisionException, ArgumentNumberException, InvalidTypeException {
		if (s.peek() == 0) {
			throw new ZeroDivisionException("Error! Cannot divide by zero.");
		} else if (s.size() < 2) {
			throw new ArgumentNumberException("Error! Division requires atleast two elements on the stack!");
		} else {
			Integer[] arr = new Integer[2];
			for (int i = 0; i < 2; ++i) {
				if (s.peek() instanceof Integer) {
					arr[i] = (Integer) s.pop();
				} else {
					throw new InvalidTypeException("Error! Division requires arguments to be a number.");
				}
			}
			s.push(arr[1]/arr[0]);
		}
	}

	/**
	 * Parses the multiplication operation by applying the '*' to the next two elements on the stack.
	 * @param  s                              	The pointer to the Stack to which the multiplication operation will be applied.
	 * @throws ArgumentNumberException        	Exception for invalid number of arguments.
	 * @throws InvalidTypeException 	Exception for invalid types of arguments.
	 */
	private static void parseMultiplication(Stack s) throws ArgumentNumberException, InvalidTypeException {
		if (s.size() < 2) {
			throw new ArgumentNumberException("Error! Multiplication requires atleast two elements on the stack!");
		} else {
			Integer[] arr = new Integer[2];
			for (int i = 0; i < 2; ++i) {
				if (s.peek() instanceof Integer) {
					arr[i] = (Integer) s.pop();
				} else {
					throw new InvalidTypeException("Error! Multiplication requires arguments to be a number.");
				}
			}
			s.push(arr[1] * arr[0]);
		}
	}

	/**
	 * Parses the MIDI operation to the next three elements on the stack.
	 * @param  s                              	The pointer to the Stack to which the multiplication operation will be applied.
	 * @throws ArgumentNumberException        	Exception for invalid number of arguments.
	 * @throws InvalidTypeException 	Exception for invalid types of arguments.
	 * @throws DataOutOfRangeException Exception for data out of normal bounds.
	 */
	private static void parseMIDI(Stack s) throws ArgumentNumberException, InvalidTypeException, DataOutOfRangeException, InvalidMidiDataException, MidiUnavailableException {
		if (s.size() < 3) {
			throw new ArgumentNumberException("Error! MIDI requires atleast three elements on the stack!");
		} else {
			Integer[] arr = new Integer[3];
			for (int i = s.size() - 1, j = 0; i >= (s.size() - 3); --i, ++j) {
				if (s.peek() instanceof Integer) {
					arr[j] = (Integer) s.get(i);
				} else {
					throw new InvalidTypeException("Error! MIDI requires arguments to be a number.");
				}
			}
			Integer instrument = arr[0];
			Integer note = arr[1];
			Integer duration = arr[2];

			System.out.println(instrument + " " + note + " " + duration);

			if ((instrument < 0) && (instrument > 127)) {
				throw new DataOutOfRangeException("Instrument must be value between 0 and 127, inclusive.");
			} else {
					System.out.println("Playing MIDI sound: duration=" + duration + ", note=" + note + ", instrument=" + instrument + "...");
			}

			/** Courtesy of HeadFirstJava */
			Sequencer player = MidiSystem.getSequencer();
			player.open();
			Sequence seq = new Sequence(Sequence.PPQ, 4);
			Track track = seq.createTrack();
			MidiEvent event = null;
			ShortMessage first = new ShortMessage();
			first.setMessage (192, 1, instrument, 0);
			MidiEvent changeInstrument = new MidiEvent (first, 1);
			track.add(changeInstrument);

			ShortMessage a = new ShortMessage();
			a.setMessage(144, 1, note, 100);
			MidiEvent noteOn = new MidiEvent(a, 1);
			track.add(noteOn);

			ShortMessage b = new ShortMessage();
			b.setMessage(128, 1, note, 100);
			MidiEvent noteOff = new MidiEvent(b, duration);
			track.add(noteOff);
			player.setSequence(seq);
			player.start();

		}
	}

	/**
	 * Parses the addition operation by applying the '+' to the next two elements on the stack.
	 * @param  s           		       The pointer to the Stack to which the addition operation will be applied.
	 * @throws ArgumentNumberException Exception for invalid number of arguments/
	 * @throws InvalidTypeException    Exception for invalid types of arguments.
	 */
	private static void parseAddition(Stack s) throws ArgumentNumberException, InvalidTypeException {
		if (s.size() < 2) {
			throw new ArgumentNumberException("Error! Addition requires atleast two elements on the stack!");
		} else {
			Integer[] arr = new Integer[2];
			for (int i = 0; i < 2; ++i) {
				if (s.peek() instanceof Integer) {
					arr[i] = (Integer) s.pop();
				} else {
					throw new InvalidTypeException("Error! Addition requires arguments to be a number.");
				}
			}
			s.push(arr[1] + arr[0]);
		}
	}
	/**
	 * Parses the subtraction operation by applying the '-' to the next two elements on the stack.
	 * @param  s           		       The pointer to the Stack to which the addition operation will be applied.
	 * @throws ArgumentNumberException Exception for invalid number of arguments/
	 * @throws InvalidTypeException    Exception for invalid types of arguments.
	 */
	private static void parseSubtraction(Stack s) throws ArgumentNumberException, InvalidTypeException {
		if (s.size() < 2) {
			throw new ArgumentNumberException("Error! Subtraction requires atleast two elements on the stack!");
		} else {
			Integer[] arr = new Integer[2];
			for (int i = 0; i < 2; ++i) {
				if (s.peek() instanceof Integer) {
					arr[i] = (Integer) s.pop();
				} else {
					throw new InvalidTypeException("Error! Subtraction requires arguments to be a number.");
				}
			}
			s.push(arr[1] - arr[0]);
		}
	}

	/**
	 * Implements an Exception that signals division by zero.
	 */
	private static class DataOutOfRangeException extends Exception {
	  protected DataOutOfRangeException() {
	    super();
	  }
	  protected DataOutOfRangeException(String s) {
	    super(s);
	  }
	}

	/**
	 * Implements an Exception that signals division by zero.
	 */
	private static class ZeroDivisionException extends Exception {
	  protected ZeroDivisionException() {
	    super();
	  }
	  protected ZeroDivisionException(String s) {
	    super(s);
	  }
	}
	/**
	 *  Implements an Exception that signals an invalid number of arguments for arithmetic operations.
	 */
	private static class ArgumentNumberException extends Exception { // inner classes can be used only after instantiating the outer class. hence the reason I use nested classes instead.
	  protected ArgumentNumberException() {
	    super();
	  }
	  protected ArgumentNumberException(String s) {
	    super(s);
	  }
	}
	/**
	 * Implements an Exception that signals division by zero.
	 */
	private static class InvalidTypeException extends Exception {
	  protected InvalidTypeException() {
	    super();
	  }
	  protected InvalidTypeException(String s) {
	    super(s);
	  }
	}

}