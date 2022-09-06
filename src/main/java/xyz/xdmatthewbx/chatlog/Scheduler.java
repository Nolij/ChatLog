package xyz.xdmatthewbx.chatlog;

import net.minecraft.client.MinecraftClient;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;

public final class Scheduler {
	public static final Scheduler INSTANCE = new Scheduler();

	private final ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<>();
	private int currentTick = 0;

	private Scheduler() {
		ClientTickEvents.END.register(this::runTasks);
	}

	/**
	 * queue a one-shot task to be executed on the server thread at the end of the current tick and capture the result
	 * in a {@link CompletableFuture}
	 *
	 * @param task the action to perform
	 */
	public <T> CompletableFuture<T> submit(Function<MinecraftClient, T> task) {
		return this.submit(task, 0);
	}

	/**
	 * queue a one time task to be executed on the server thread and capture the result in a {@link CompletableFuture}
	 *
	 * @param delay how many ticks in the future this should be called, where 0 means at the end of the current tick
	 * @param task the action to perform
	 */
	public <T> CompletableFuture<T> submit(Function<MinecraftClient, T> task, int delay) {
		var future = new CompletableFuture<T>();
		this.submit(client -> {
			var result = task.apply(client);
			future.complete(result);
		}, delay);
		return future;
	}

	/**
	 * queue a one-shot task to be executed on the server thread at the end of the current tick
	 *
	 * @param task the action to perform
	 */
	public void submit(Consumer<MinecraftClient> task) {
		this.submit(task, 0);
	}

	/**
	 * queue a one time task to be executed on the server thread
	 *
	 * @param delay how many ticks in the future this should be called, where 0 means at the end of the current tick
	 * @param task the action to perform
	 */
	public void submit(Consumer<MinecraftClient> task, int delay) {
		this.taskQueue.add(new OneshotTask(task, this.currentTick + delay));
	}

	/**
	 * schedule a repeating task that is executed infinitely every n ticks
	 *
	 * @param task the action to perform
	 * @param delay how many ticks in the future this event should first be called
	 * @param interval the number of ticks in between each execution
	 */
	public void repeat(Consumer<MinecraftClient> task, int delay, int interval) {
		this.repeatWhile(task, null, delay, interval);
	}

	/**
	 * repeat the given task until the predicate returns false
	 *
	 * @param task the action to perform
	 * @param condition whether or not to reschedule the task again, with the parameter being the current tick
	 * @param delay how many ticks in the future this event should first be called
	 * @param interval the number of ticks in between each execution
	 */
	public void repeatWhile(Consumer<MinecraftClient> task, IntPredicate condition, int delay, int interval) {
		int beginTime = this.currentTick + delay;
		this.enqueue(new DoWhileTask(task, condition, beginTime, interval));
	}

	private void enqueue(Task task) {
		this.taskQueue.add(task);
	}

	private void runTasks(MinecraftClient client) {
		this.currentTick++;

		this.taskQueue.removeIf(task -> task.tryRun(client, currentTick));
	}

	private interface Task {
		boolean tryRun(MinecraftClient client, int time);
	}

	private record OneshotTask(Consumer<MinecraftClient> action, int time) implements Task {
		@Override
		public boolean tryRun(MinecraftClient client, int time) {
			if (time >= this.time) {
				this.action.accept(client);
				return true;
			}
			return false;
		}
	}

	private static class DoWhileTask implements Task {
		private final Consumer<MinecraftClient> task;
		private final IntPredicate condition;
		private final int interval;

		private int nextTime;

		private DoWhileTask(Consumer<MinecraftClient> task, IntPredicate condition, int beginTime, int interval) {
			this.task = task;
			this.condition = condition;
			this.nextTime = beginTime;
			this.interval = interval;
		}

		@Override
		public boolean tryRun(MinecraftClient client, int time) {
			if (time >= this.nextTime) {
				this.task.accept(client);
				this.nextTime = time + this.interval;

				return !this.shouldRepeat(time);
			}

			return false;
		}

		private boolean shouldRepeat(int predicate) {
			IntPredicate condition = this.condition;
			return condition == null || condition.test(predicate);
		}
	}
}
