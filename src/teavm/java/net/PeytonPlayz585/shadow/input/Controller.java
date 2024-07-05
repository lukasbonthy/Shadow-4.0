package net.PeytonPlayz585.shadow.input;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.browser.Navigator;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.gamepad.GamepadEvent;
import org.teavm.jso.gamepad.Gamepad;

public class Controller {
	
	static List<Integer> connectedControllers = new ArrayList<Integer>();
	
	private static double dx = 0;
	private static double dy = 0;
	
	private static boolean forward = false;
	private static boolean backwards = false;
	private static boolean left = false;
	private static boolean right = false;
	
	private static double threshold = 0.3;
	//Fixes 'slight' issues of stick drift people have been complaining about
	private static double cameraThreshold = 4.0;
	
	private static int activeController = -1;
	private static ButtonState[] states = new ButtonState[30];
	
	public static final int getDX() {
		if(dx < 0.0) {
			if(dx < -cameraThreshold) {
				return (int)dx;
			}
		} else if(dx > 0.0) {
			if(dx > cameraThreshold) {
				return (int)dx;
			}
		}
		return 0;
	}
	
	public static final int getDY() {
		if(dy < 0.0) {
			if(dy < -cameraThreshold) {
				return (int)dy;
			}
		} else if(dy > 0.0) {
			if(dy > cameraThreshold) {
				return (int)dy;
			}
		}
		return 0;
	}
	
	public static boolean forward() {
		return forward;
	}
	
	public static boolean backwards() {
		return backwards;
	}
	
	public static boolean left() {
		return left;
	}
	
	public static boolean right() {
		return right;
	}
	
	public static boolean jump() {
		return isButtonPressed(1) || isButtonDown(1);
	}
	
	public static boolean crouch() {
		return isButtonPressed(0) || isButtonDown(0) || isButtonPressed(11) || isButtonDown(11);
	}
	
	public static boolean sprint() {
		return isButtonPressed(10) || isButtonDown(10);
	}
	
	public static boolean itemChangeLeft() {
		return states[4] == ButtonState.PRESSED;
	}
	
	public static boolean itemChangeRight() {
		return states[5] == ButtonState.PRESSED;
	}
	
	public static boolean inventory() {
		return states[2] == ButtonState.PRESSED || states[3] == ButtonState.PRESSED;
	}
	
	public static boolean togglePerspective() {
		return states[12] == ButtonState.PRESSED;
	}
	
	public static boolean playerList() {
		return states[15] == ButtonState.HELD;
	}
	
	public static boolean smoothCamera() {
		return states[14] == ButtonState.PRESSED;
	}
	
	public static boolean dropItem() {
		return states[13] == ButtonState.PRESSED;
	}
	
	public static boolean isButtonPressed(int i) {
		return states[i] == ButtonState.PRESSED;
	}
	
	public static boolean isButtonDown(int i) {
		return states[i] == ButtonState.HELD;
	}

	private static void updateAxes(Gamepad gamePad) {
		double[] axes = gamePad.getAxes();
		int multiplier = 50;
		
		/* 
		 * Not sure if these are controller specific
		 * ¯\_(ツ)_/¯
		 */
		dx = axes[2] * multiplier;
		
		/*
		 * Idk if it's inverted for all controllers
		 * or just the one I'm using
		 */
		dy = -axes[3] * multiplier;
		
		forward = axes[1] < -threshold;
		backwards = axes[1] > threshold;
		left = axes[0] < -threshold;
		right = axes[0] > threshold;
	}
	
	private static void updateButtons(Gamepad gamePad, int index) {
		for(int i = 0; i < gamePad.getButtons().length; i++) {
			if(gamePad.getButtons()[i].isPressed()) {
				if(activeController != index) {
					activeController = index;
					resetButtonStates();
				}
				
				if(states[i] == ButtonState.PRESSED) {
					states[i] = ButtonState.HELD;
				} else {
					if(!(states[i] == ButtonState.HELD)) {
						states[i] = ButtonState.PRESSED;
					}
				}
			} else if(!gamePad.getButtons()[i].isPressed() && index == activeController) {
				states[i] = ButtonState.DEFAULT;
			}
		}
	}
	
	public static void tick() {
		for(Integer index : connectedControllers) {
			Gamepad gamePad = getGamepad(index);
			updateAxes(gamePad);
			updateButtons(gamePad, index);
		}
	}
	
	private static Gamepad getGamepad(int index) {
		return Navigator.getGamepads()[index];
	}
	
	private static void resetButtonStates() {
		for(int i = 0; i < states.length; i++) {
			states[i] = ButtonState.DEFAULT;
		}
	}
	
	private static enum ButtonState {
		DEFAULT /* not pressed */, PRESSED, HELD
	}
	
	static {
		Window.current().addEventListener("gamepadconnected", new EventListener<GamepadEvent>() {
			@Override
			public void handleEvent(GamepadEvent arg0) {
				connectedControllers.add(arg0.getGamepad().getIndex());
				System.out.println("Controller connected!");
			}
		});
		
		Window.current().addEventListener("gamepaddisconnected", new EventListener<GamepadEvent>() {
			@Override
			public void handleEvent(GamepadEvent arg0) {
				int index = arg0.getGamepad().getIndex();
				if(connectedControllers.contains(index)) {
					connectedControllers.remove(index);
					System.out.println("Controller disconnected!");
				}
			}
		});
		
		resetButtonStates();
	}
}