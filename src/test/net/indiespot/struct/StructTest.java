package test.net.indiespot.struct;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import net.indiespot.struct.cp.CopyStruct;
import net.indiespot.struct.cp.Struct;
import net.indiespot.struct.cp.StructField;
import net.indiespot.struct.cp.StructType;
import net.indiespot.struct.cp.TakeStruct;
import net.indiespot.struct.runtime.IllegalStackAccessError;
import net.indiespot.struct.runtime.StructAllocationStack;
import net.indiespot.struct.runtime.StructGC;
import net.indiespot.struct.runtime.StructMemory;
import net.indiespot.struct.runtime.StructThreadLocalStack;
import net.indiespot.struct.runtime.StructUnsafe;
import net.indiespot.struct.runtime.SuspiciousFieldAssignmentError;
import net.indiespot.struct.runtime.StructGC.GcStats;
import net.indiespot.struct.transform.StructEnv;

public class StructTest {
	public static class WideHandleTest {
		public static void test() {
			ByteBuffer bb = ByteBuffer.allocateDirect(1024);
			long addr = StructUnsafe.getBufferBaseAddress(bb);

			Vec3 vec0 = Struct.fromPointer(addr);
			vec0.x = 45.67f;
			long pntr = Struct.getPointer(vec0);
			assert addr == pntr;

			Vec3 vec1 = Struct.fromPointer(addr + 12);
			vec1.x = 13.37f;

			Vec3[] arr = Struct.fromPointer(addr, 12, 10);
			assert arr[0].x == 45.67f;
			assert arr[1].x == 13.37f;

			arr = Struct.fromPointer(addr, Struct.sizeof(Vec3.class), 10);
			assert arr[0].x == 45.67f;
			assert arr[1].x == 13.37f;

			arr = Struct.fromPointer(addr, Vec3.class, 10);
			assert arr[0].x == 45.67f;
			assert arr[1].x == 13.37f;

			assert Struct.index(vec0, Vec3.class, 0).x == 45.67f;
			assert Struct.index(vec0, Vec3.class, 1).x == 13.37f;
		}
	}

	public static class WideHandleParamsLocalsTest {
		public static void test() {
			test1(new Vec3(), new Vec3());
			test2(new Object(), new Vec3(), new Vec3());
			test3(new Object(), new Vec3(), new Vec3());
			test4(new Vec3(), new Object(), new Vec3());
			test5(new Vec3(), new Vec3(), new Object());
			test6(new Vec3(), new Vec3(), new Object());
			Vec3 a = test7(new Vec3(), new Vec3(), new Object());
			Vec3 b = test8(new Vec3(), new Vec3(), new Object());
			// test8(new Vec3(), new Vec3(), new Vec3());
			// test4(Struct.nullStruct(Vec3.class), null,
			// Struct.nullStruct(Vec3.class));
			test8(new Vec3(), new Vec3(), null);
			test8(new Vec3(), Struct.nullStruct(Vec3.class), null);
			// test8(new Vec3(), null, null);
		}

		public static void test1(Vec3 a, Vec3 b) {
			Vec3 c = Struct.fromPointer(0L);
			Vec3 d = Struct.fromPointer(0L);
		}

		public static void test2(Object obj, Vec3 a, Vec3 b) {
			Vec3 c = Struct.fromPointer(0L);
			Vec3 d = Struct.fromPointer(0L);
		}

		public static void test3(Object obj, Vec3 a, Vec3 b) {
			long pa = Struct.getPointer(a);
			long pb = Struct.getPointer(b);
			assert (pa != pb);

			a = b;
			assert pb == Struct.getPointer(a);
			assert pb == Struct.getPointer(b);
		}

		public static void test4(Vec3 a, Object obj, Vec3 b) {
			long pa = Struct.getPointer(a);
			long pb = Struct.getPointer(b);
			assert (pa != pb);

			a = b;
			assert pb == Struct.getPointer(a);
			assert pb == Struct.getPointer(b);
		}

		public static void test5(Vec3 a, Vec3 b, Object obj) {
			long pa = Struct.getPointer(a);
			long pb = Struct.getPointer(b);
			assert (pa != pb);

			a = b;
			assert pb == Struct.getPointer(a);
			assert pb == Struct.getPointer(b);
		}

		public static void test6(Vec3 a, Vec3 b, Object obj) {
			long pa = Struct.getPointer(a);
			long pb = Struct.getPointer(b);
			assert (pa != pb);

			b = a;
			assert pa == Struct.getPointer(a);
			assert pa == Struct.getPointer(b);
		}

		@TakeStruct
		public static Vec3 test7(Vec3 a, Vec3 b, Object obj) {
			return a;
		}

		@CopyStruct
		public static Vec3 test8(Vec3 a, Vec3 b, Object obj) {
			Vec3 c = new Vec3();
			return c;
		}
	}

	public static class EmbeddedArrayTest {
		public static void test() {
			EmbeddedArray ea = new EmbeddedArray();

			ea.farr[0] = 12.34f;
			ea.farr[1] = 23.45f;
			assert ea.farr[0] == 12.34f;
			assert ea.farr[1] == 23.45f;
			assert ea.farr[0] == 12.34f;
			assert ea.farr[1] == 23.45f;

			ea.darr[0] = 10012.34;
			ea.darr[1] = 10023.45;
			assert ea.darr[0] == 10012.34;
			assert ea.darr[1] == 10023.45;
			assert ea.darr[0] == 10012.34;
			assert ea.darr[1] == 10023.45;

			assert ea.farr[0] == 12.34f;
			assert ea.farr[1] == 23.45f;
		}
	}

	@StructType
	public static class EmbeddedArray {
		@StructField(elements = 5)
		public float[] farr;

		@StructField(elements = 3)
		public double[] darr;
	}

	public static class TestStackOps {
		public static void test1() {
			Vec3 vec = new Vec3();
			vec.x = vec.y = vec.z = 1;
		}
	}

	public static interface Interf {
		public void i(Vec3 a);
	}

	public static abstract class Abstract {
		public abstract void i(Vec3 a);

		public void a(Vec3 a) {
			i(a);
		}
	}

	public static class TestInterf {
		public static void test1() {
			new Interf() {
				@Override
				public void i(Vec3 a) {
					// TODO Auto-generated method stub

				}
			}.i(new Vec3(1, 2, 3));

			new Abstract() {
				@Override
				public void i(Vec3 a) {
					System.out.println(a.toString());
				}
			}.a(new Vec3(1, 2, 3));
		}
	}

	public static class TestLambda {
		private static List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

		public static void test() {
			new Vec3(); // triggers transform

			list.stream().filter(i -> {
				return i % 2 == 0;
			}).forEach(i -> System.out.println(i));
		}
	}

	public static void main(String[] args) {
		StructGC.addListener(new StructGC.GcInfo() {

			@Override
			public void onGC(GcStats stats) {
				System.out.println("LibStruct GC: id=" + stats.runId + ", freed=" + stats.freed + ", remaining=" + stats.remaining + ", collected regions: " + stats.collectedRegionCount + ", took: " + stats.tookNanos / 1000 / 1000 + "ms");
			}

			@Override
			public void onStress() {
				//
			}

			@Override
			public void onPanic() {
				//
			}
		});

		if (false)
			TestCollectionAPI.test();
		if (false)
			TestDuplicateOverloadedMethod.test();

		if (true) {
			// TestAllocPerformance.test();
			// return;
		}

		TestStructEnv.test();
		TestSuspiciousFieldAssignment.test();
		WideHandleTest.test();
		WideHandleParamsLocalsTest.test();
		EmbeddedArrayTest.test();
		TestStackOps.test1();
		TestInterf.test1();
		TestLambda.test();

		if (true) {
			TestCalloc.test();
			TestAlignment.test();
			TestSwap.test();

			TestSizeof.test();
			TestNull.test();
			TestOneInstance.test();
			TestOneInstanceNull.test();
			TestOneInstanceNullRef.test();
			TestOneInstanceInstanceof.test();
			TestTwoInstances.test();

			TestFields.test();
			TestSum.test();

			TestSetter.test();
			TestMethodPass.test();

			TestIfFlow.test();
			TestLoopFlow.test();

			TestConstructor.test();
			TestTryFinally.test();
			TestTryCatchFinally.test();

			TestArray.test();
			TestMapping.test();
			TestInterleavedMapping.test();

			TestInstanceMethod.test();
			TestStructReturnType.test();

			if (StructEnv.SAFETY_FIRST) {
				try {
					TestStack.test();
					throw new IllegalStateException();
				} catch (IllegalStackAccessError expected) {
					// okay!
				}
			}

			TestStructField.test();
			TestStructWithStructField.test();
			//
			TestMalloc.test();

			TestCustomStack.test();
			TestCopy.test();
			TestView.test();
			TestSwitch.test();
			TestIndex.test();
		}

		// ParticleTestStruct.main(args);

		// TestPerformance.test();
		// TheAgentD.main(args); // TestMultiThreadedAllocation.test();
		TestMalloc.testBlockingQueueProducerConsumer();
		// TestAllocPerformance.test();

		TestEmbedStruct.test();
		TestFromPointer.test();

		TestRealloc.test();
		TestLargeAlloc.test();

		System.out.println("awaiting gc...");
		while (StructGC.getHandleCount() != 0) {
			Thread.yield();
		}

		System.out.println("done");
	}

	public static class TestLargeAlloc {
		public static void test() {
			int count = Integer.MAX_VALUE / 3 / (Struct.sizeof(Vec3.class) - 2);
			System.out.println("count=" + count);
			System.out.println("sizeof=" + Struct.sizeof(Vec3.class));
			Vec3 base = Struct.mallocArrayBase(Vec3.class, count);
			for (int i = 1; i < count; i++) {
				Vec3 v1 = Struct.index(base, Vec3.class, i - 1);
				Vec3 v2 = Struct.index(base, Vec3.class, i - 0);
				v2.mul(v1);
				v1.mul(v2);
				long p1 = Struct.getPointer(v1);
				long p2 = Struct.getPointer(v2);
				assert (p2 - p1) == Struct.sizeof(Vec3.class);
			}
			Struct.free(base);
		}
	}

	public static class TestRealloc {
		public static void test() {
			Vec3[] arr = Struct.mallocArray(Vec3.class, 13);
			assert arr.length == 13;

			arr[4].x = 13.14f;
			arr[7].y = 17.13f;

			arr = Struct.reallocArray(Vec3.class, arr, 13);
			assert arr.length == 13;
			assert arr[4].x == 13.14f;
			assert arr[7].y == 17.13f;

			arr = Struct.reallocArray(Vec3.class, arr, 5);
			assert arr.length == 5;
			assert arr[4].x == 13.14f;

			arr = Struct.reallocArray(Vec3.class, arr, 8);
			assert arr.length == 8;
			assert arr[4].x == 13.14f;

			Struct.free(arr);
		}
	}

	public static class TestCollectionAPI {
		public static void test() {
			List<Vec3> vectors = new ArrayList<>();
			// List<Object> vectors = new ArrayList<>();
			vectors.add(new Vec3());
		}
	}

	public static class TestEmbeddedArrayUsage {
		public static void test() {
			// ArrayEmbed ae = new ArrayEmbed();
			// paramValue(ae.iarr); // this will fail
		}

		public static void paramValue(int[] arr) {
			// ok
		}

		public static int[] returnValue() {
			// ArrayEmbed ae = new ArrayEmbed();
			// return ae.iarr; // this will fail
			return null;
		}

		public static void reassign() {
			// ArrayEmbed ae = new ArrayEmbed();
			// ae.iarr = new int[4]; // this will fail
		}
	}

	public static class TestFromPointer {
		public static void test() {
			ByteBuffer bb = ByteBuffer.allocateDirect(234);
			long addr = StructUnsafe.getBufferBaseAddress(bb);
			Vec3 v1 = Struct.fromPointer(addr + 0 * Struct.sizeof(Vec3.class));
			Vec3 v2 = Struct.fromPointer(addr + 1 * Struct.sizeof(Vec3.class));
			assert Struct.getPointer(v1) == (addr + 0 * Struct.sizeof(Vec3.class));
			assert Struct.getPointer(v2) == (addr + 1 * Struct.sizeof(Vec3.class));
			bb.clear(); // prevent untimely GC
		}
	}

	public static class TestDuplicateOverloadedMethod {
		public static void test() {
			test(1);
			test(new Vec3());
			test(new Ship());
		}

		public static void test(int val) {

		}

		public static void test(Vec3 vec3) {

		}

		public static void test(Ship ship) {

		}
	}

	public static class TestSuspiciousFieldAssignment {
		public static Vec3 field;

		public static void test() {
			try {
				field = new Vec3();
				if (StructEnv.SAFETY_FIRST)
					assert false;
			} catch (SuspiciousFieldAssignmentError err) {
				assert true;
			}

			field = Struct.malloc(Vec3.class);

			Ship ship = new Ship();
			ship.pos = field;
			ship.pos = new Vec3();

			ship = Struct.calloc(Ship.class);
			ship.pos = field;
			try {
				ship.pos = new Vec3();
				if (StructEnv.SAFETY_FIRST)
					assert false;
			} catch (SuspiciousFieldAssignmentError err) {
				assert true;
			}

			Struct.free(field);
			Struct.free(ship);
		}
	}

	public static class TestSwap {
		public static void test() {
			Vec3 a = new Vec3(1.20f, 2.30f, 3.40f);
			Vec3 b = new Vec3(1.02f, 2.03f, 3.04f);
			// System.out.println(a.toString());
			// System.out.println(b.toString());

			Struct.swap(Vec3.class, a, b);
			// System.out.println(a.toString());
			// System.out.println(b.toString());
		}
	}

	public static class TestIndex {
		public static void test() {
			Vec3[] arr = new Vec3[123];

			arr[0].set(1.20f, 2.30f, 3.40f);
			arr[1].set(1.02f, 2.03f, 3.04f);

			assert Struct.index(arr[1], Vec3.class, -1) == arr[0];
			assert Struct.index(arr[1], Vec3.class, +1) == arr[2];
		}
	}

	public static class TestEmbedStruct {
		public static void test() {
			StructEmbed em1 = new StructEmbed();
			StructEmbed em2 = new StructEmbed();
			assert (Struct.getPointer(em2) - Struct.getPointer(em1)) == Struct.sizeof(StructEmbed.class);

			assert (Struct.getPointer(em1) - Struct.getPointer(em1.pos)) == 0;
			assert (Struct.getPointer(em1.vel) - Struct.getPointer(em1.pos)) == Struct.sizeof(Vec3.class);

			assert (Struct.getPointer(em2) - Struct.getPointer(em2.pos)) == 0;
			assert (Struct.getPointer(em2.vel) - Struct.getPointer(em2.pos)) == Struct.sizeof(Vec3.class);
		}
	}

	public static class TestSwitch {
		public static void test() {
			new Vec3();

			switch (4) {
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			}

			switch (4) {
			case 1003:
				break;
			case 2003:
				break;
			case 3003:
				break;
			}
		}
	}

	public static class TestCalloc {
		public static void test() {
			Vec3 a = Struct.malloc(Vec3.class);
			Vec3 b = Struct.calloc(Vec3.class);

			assert (a.x != 0.0f);
			assert (a.y != 0.0f);
			assert (a.z != 0.0f);

			assert (b.x == 0.0f);
			assert (b.y == 0.0f);
			assert (b.z == 0.0f);

			Struct.free(a);
			Struct.free(b);
		}
	}

	public static class TestView {
		public static void test() {
			PosVelRef ref = new PosVelRef();
			ref.pos = new Vec3();
			ref.vel = new Vec3();
			ref.pos.set(13, 14, 15);
			ref.vel.set(16, 17, 18);

			PosVelView embed = new PosVelView();
			embed.pos().set(19, 20, 21);
			embed.vel().set(22, 23, 24);
		}
	}

	public static class TestCopy {
		public static void test() {
			Vec3 a = Struct.malloc(Vec3.class);
			a.x = 12.34f;
			a.y = 23.45f;
			a.z = 34.56f;

			Vec3 b = Struct.malloc(Vec3.class);
			b.x = 00.00f;
			b.y = 00.00f;
			b.z = 00.00f;

			assert (a.x == 12.34f);
			assert (b.x == 00.00f);
			Struct.copy(Vec3.class, a, b);
			assert (a.x == 12.34f);
			assert (b.x == 12.34f);

			Struct.free(a);
			Struct.free(b);
		}
	}

	public static class TestCustomStack {
		public static void test() {
			StructAllocationStack stack = Struct.createStructAllocationStack(1024);

			stack.save();

			Vec3 v1 = Struct.stackAlloc(stack, Vec3.class);
			Vec3 v2 = Struct.stackAlloc(stack, Vec3.class);

			stack.restore();

			Vec3 v1b = Struct.stackAlloc(stack, Vec3.class);
			Vec3 v2b = Struct.stackAlloc(stack, Vec3.class);

			assert (v1 == v1b);
			assert (v2 == v2b);

			Struct.discardStructAllocationStack(stack);
		}
	}

	public static class TestAllocPerformance {
		public static void test() {
			final int allocCount = 10_000_000;

			StructAllocationStack sas = Struct.createStructAllocationStack(allocCount * Struct.sizeof(Vec3.class) + 100);

			for (int k = 0; k < 10; k++) {
				long tm2 = System.nanoTime();
				for (int i = 0; i < allocCount; i++)
					instance();
				long tm1 = System.nanoTime();
				sas.save();
				for (int i = 0; i < allocCount; i++)
					stackAlloc(sas);
				sas.restore();
				long t0 = System.nanoTime();
				for (int i = 0; i < allocCount; i++)
					stackAlloc1();
				long t1 = System.nanoTime();
				for (int i = 0; i < allocCount; i += 100)
					stackAlloc1N(100);
				long t2 = System.nanoTime();
				for (int i = 0; i < allocCount; i += 100)
					stackAlloc10N(10);
				long t3 = System.nanoTime();
				for (int i = 0; i < allocCount; i += 100)
					stackAllocArray(100);
				long t4 = System.nanoTime();
				for (int i = 0; i < allocCount; i++)
					memoryAlloc();
				long t5 = System.nanoTime();
				for (int i = 0; i < allocCount; i += 100)
					memoryAllocArray(100);
				long t6 = System.nanoTime();
				for (int i = 0; i < allocCount; i += 100)
					memoryAllocArrayBulkFree(100);
				long t7 = System.nanoTime();
				for (int i = 0; i < allocCount; i += 100)
					memoryAllocArrayBaseFree(100);
				long t8 = System.nanoTime();

				long tInstance1 = (tm1 - tm2) / 1000L;
				long tStackAllocS = (t0 - tm1) / 1000L;
				long tStackAlloc1 = (t1 - t0) / 1000L;
				long tStackAlloc1N = (t2 - t1) / 1000L;
				long tStackAlloc10N = (t3 - t2) / 1000L;
				long tStackAllocArr = (t4 - t3) / 1000L;
				long tMemoryAllocAndFree = (t5 - t4) / 1000L;
				long tMemoryAllocAndFreeArr = (t6 - t5) / 1000L;
				long tMemoryAllocAndFree2Arr = (t7 - t6) / 1000L;
				long tMemoryAllocAndFree3Arr = (t8 - t7) / 1000L;

				System.out.println();
				System.out.println("tInstance1      \t" + tInstance1 / 1000 + "ms \t" + (int) (allocCount / (double) tInstance1) + "M/s");
				System.out.println("tStackAllocS    \t" + tStackAllocS / 1000 + "ms \t" + (int) (allocCount / (double) tStackAllocS) + "M/s");
				System.out.println("tStackAlloc1    \t" + tStackAlloc1 / 1000 + "ms \t" + (int) (allocCount / (double) tStackAlloc1) + "M/s");
				System.out.println("tStackAlloc1N   \t" + tStackAlloc1N / 1000 + "ms   \t" + (int) (allocCount / (double) tStackAlloc1N) + "M/s");
				System.out.println("tStackAlloc10N  \t" + tStackAlloc10N / 1000 + "ms  \t" + (int) (allocCount / (double) tStackAlloc10N) + "M/s");
				System.out.println("tStackAllocArr  \t" + tStackAllocArr / 1000 + "ms  \t" + (int) (allocCount / (double) tStackAllocArr) + "M/s");
				System.out.println("tMemoryAllocFree     \t" + tMemoryAllocAndFree / 1000 + "ms    \t" + (int) (allocCount / (double) tMemoryAllocAndFree) + "M/s");
				System.out.println("tMemoryAllocFreeArr  \t" + tMemoryAllocAndFreeArr / 1000 + "ms \t" + (int) (allocCount / (double) tMemoryAllocAndFreeArr) + "M/s");
				System.out.println("tMemoryAllocFreeArr2 \t" + tMemoryAllocAndFree2Arr / 1000 + "ms \t" + (int) (allocCount / (double) tMemoryAllocAndFree2Arr) + "M/s");
				System.out.println("tMemoryAllocFreeArr3 \t" + tMemoryAllocAndFree3Arr / 1000 + "ms    \t" + (int) (allocCount / (double) tMemoryAllocAndFree3Arr) + "M/s");
			}

			Struct.discardStructAllocationStack(sas);
		}

		private static void instance() {
			new NormalVec3(); // HotSpot might remove this
		}

		private static void stackAlloc(StructAllocationStack stack) {
			Struct.stackAlloc(stack, Vec3.class).set(0.0f, 0.0f, 0.0f);
		}

		@SuppressWarnings("unused")
		private static void stackAlloc1() {
			Vec3 vec = new Vec3(); // HotSpot will _not_ remove this
		}

		@SuppressWarnings("unused")
		private static void stackAlloc1N(int n) {
			for (int i = 0; i < n; i++) {
				Vec3 vec = new Vec3(); // HotSpot will _not_ remove this
			}
		}

		private static void stackAlloc10N(int n) {
			for (int i = 0; i < n; i++) {
				new Vec3(); // HotSpot will _not_ remove this
				new Vec3();
				new Vec3();
				new Vec3();
				new Vec3();
				new Vec3();
				new Vec3();
				new Vec3();
				new Vec3();
				new Vec3();
			}
		}

		@SuppressWarnings("unused")
		private static void stackAllocArray(int n) {
			Vec3[] arr = new Vec3[n]; // HotSpot will _not_ remove this
		}

		private static void memoryAlloc() {
			Struct.free(Struct.malloc(Vec3.class));
		}

		private static void memoryAllocArray(int n) {
			for (Vec3 vec : Struct.mallocArray(Vec3.class, n))
				Struct.free(vec);
		}

		private static void memoryAllocArrayBulkFree(int n) {
			Struct.free(Struct.mallocArray(Vec3.class, n));
		}

		private static void memoryAllocArrayBaseFree(int n) {
			Struct.free(Struct.mallocArrayBase(Vec3.class, n));
		}

	}

	public static class TestMalloc {
		public static void test() {
			for (int i = 0; i < 4; i++) {
				Vec3 vec1 = Struct.malloc(Vec3.class);
				Vec3 vec2 = Struct.malloc(Vec3.class);

				Struct.free(vec1);
				Struct.free(vec2);
			}

			Vec3[] vecs = Struct.mallocArray(Vec3.class, 7);
			for (Vec3 vec : vecs) {
				Struct.free(vec);
			}

			vecs = Struct.mallocArray(Vec3.class, 100);
			for (Vec3 vec : vecs) {
				Struct.free(vec);
			}

			Struct.free(Struct.mallocArray(Vec3.class, 100));
		}

		private static class Vec3BlockingQueue {
			private Vec3[] queue;
			private int size;

			public Vec3BlockingQueue(int cap) {
				queue = Struct.nullArray(Vec3.class, cap);
			}

			public synchronized void push(Vec3 vec) {
				while (size == queue.length) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// ignore
					}
				}
				queue[size++] = vec;
				this.notify();
				// System.out.println("pushed");
			}

			@TakeStruct
			public synchronized Vec3 pop() {
				while (size == 0) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// ignore
					}
				}
				Vec3 vec = queue[--size];
				this.notify();
				// System.out.println("popped");
				return vec;
			}

			@TakeStruct
			public synchronized Vec3 poll(long timeout) {
				final long started = System.currentTimeMillis();
				while (size == 0) {
					try {
						this.wait(timeout);
					} catch (InterruptedException e) {
						// ignore
					}

					if (size == 0) {
						if (System.currentTimeMillis() - started > timeout) {
							return Struct.nullStruct(Vec3.class);
						}
					}
				}
				Vec3 vec = queue[--size];
				this.notify();
				// System.out.println("polled: " + vec);
				return vec;
			}
		}

		public static void testBlockingQueueProducerConsumer() {

			int start = StructGC.getHandleCount();
			if (start != 0)
				throw new IllegalStateException("StructGC.getHandleCount=" + start);

			final int itemCountPerProducer = 1_000_000;
			final long pollTimeout = 5_000;
			final int queueCount = 4;
			for (int q = 0; q < queueCount; q++) {
				Vec3BlockingQueue queue = new Vec3BlockingQueue(100_000);

				for (int i = 0; i < 8; i++)
					createProducer(queue, itemCountPerProducer);

				for (int i = 0; i < 32; i++)
					createConsumer(queue, pollTimeout);
			}

			StructGC.discardThreadLocal();

			try {
				Thread.sleep(pollTimeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			do {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (StructGC.getHandleCount() > start);
		}

		private static void createProducer(final Vec3BlockingQueue queue, final int items) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < items; i++) {
						queue.push(Struct.malloc(Vec3.class));
					}

					StructGC.discardThreadLocal();
				}
			}).start();
		}

		private static void createConsumer(final Vec3BlockingQueue queue, final long pollTimeout) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						Vec3 item = queue.poll(pollTimeout);
						if (item == null)
							break;

						Struct.free(item);
					}
				}
			}).start();
		}
	}

	public static class TestAlignment {
		public static void test() {
			for (int alignment = 4; alignment <= 4096; alignment <<= 1) {
				Vec3 v1 = Struct.malloc(Vec3.class, alignment);
				Vec3 v2 = Struct.malloc(Vec3.class, alignment);
				Vec3 v3 = Struct.malloc(Vec3.class, alignment);

				assert (Struct.getPointer(v1) % alignment == 0);
				assert (Struct.getPointer(v2) % alignment == 0);
				assert (Struct.getPointer(v3) % alignment == 0);

				Struct.free(v1);
				Struct.free(v2);
				Struct.free(v3);
			}
		}
	}

	public static class TestSizeof {
		public static void test() {
			int sizeofVec3 = Struct.sizeof(Vec3.class);
			int sizeofShip = Struct.sizeof(Ship.class);

			assert (sizeofVec3 == 12);
			assert (sizeofShip == 12);
		}
	}

	public static class TestStructWithStructField {
		public static void test() {
			Ship ship = new Ship();
			assert (ship.id == 100002);
			ship.id++;
			assert (ship.id == 100003);
			assert (ship.pos == null);

			System.out.println("--");
			System.out.println("ship @ " + Struct.getPointer(ship));
			Vec3 pos = Struct.malloc(Vec3.class);
			System.out.println("pos @ " + Struct.getPointer(pos));
			ship.pos = pos;
			System.out.println("pos @ " + Struct.getPointer(pos));
			System.out.println("ship.pos @ " + Struct.getPointer(ship.pos));

			assert (ship.pos != null);
			Struct.free(ship.pos);
			ship.pos = Struct.nullStruct(Vec3.class);

			ship.pos = new Vec3(); // SuspiciousFieldAssignmentError
		}
	}

	public static class TestStructEnv {
		public static void test() {
			test0();
			test1();
			test2();
			test3();
			test4();
		}

		public static void test0() {
			try {
				assert false;

				throw new IllegalStateException("asserts must be enabled");
			} catch (AssertionError err) {
				System.out.println("StructTest: asserts are enabled.");
			}
		}

		public static void test1() {
			assert Struct.isReachable(null) == false;
		}

		public static void test2() {
			assert Struct.isReachable(new Vec3()) == true;
		}

		public static void test3() {
			assert Struct.getPointer(new Vec3()) > 0L;
		}

		public static void test4() {
			Class<?> typ1 = String.class;
			// Class<?> typ2 = Vec3.class;

			System.out.println(typ1);
			// System.out.println("x="+typ2);
			// System.exit(-1);
		}
	}

	public static class TestMultiThreadedAllocation {
		public static void test() {
			Thread[] ts = new Thread[8];
			for (int i = 0; i < ts.length; i++) {
				ts[i] = new Thread(new Runnable() {
					@Override
					public void run() {
						TheAgentD.main(new String[0]);
					}
				});
			}

			try {
				for (int i = 0; i < ts.length; i++)
					ts[i].start();
				for (int i = 0; i < ts.length; i++)
					ts[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static class TestNull {
		public static void test() {
			new Vec3();
			Vec3 vec = null;
			vec = new Vec3();
			vec = null;
			vec = new Vec3();

			// if(Math.random() < 0.5)
			// vec = new Vec3();
			// else
			// vec = null;
			// test(vec);
		}

		// private static void test(Vec3 nullStruct) {
		// System.out.println(nullStruct);
		// }
	}

	public static class TestStructField {
		public static void test() {
			new TestStructField().testInstance();
			TestStructField.testStatic();
			TestStructField.testStatic2();
		}

		public Vec3 vec1;

		public void testInstance() {
			vec1 = Struct.calloc(Vec3.class);
			vec1.x = 43.21f;
			Vec3 that = vec1;
			vec1 = that;
			assert (vec1.x == 43.21f);
			assert (that.x == 43.21f);
			Struct.free(vec1);
		}

		public static Vec3 vec2;
		public static Vec3[] arr;

		public static void testStatic() {
			vec2 = Struct.calloc(Vec3.class);

			vec2.x = 12.34f;
			Vec3 that = vec2;
			vec2 = that;
			assert (vec2.x == 12.34f);
			assert (that.x == 12.34f);

			arr = Struct.mallocArray(Vec3.class, 13);
		}

		public static void testStatic2() {
			vec2.x = 4;
			Struct.free(vec2);

			arr[0].x = 5;

			for (Vec3 item : arr)
				Struct.free(item);
		}
	}

	public static class TestMapping {
		public static void test() {
			int alignMargin = 4 - 1;
			int sizeof = 3 << 2;
			int count = 10;
			ByteBuffer bb = ByteBuffer.allocateDirect(count * sizeof + alignMargin);
			StructMemory.alignBuffer(bb, StructMemory.JVMWORD_ALIGNMENT);
			Vec3[] mapped = Struct.map(Vec3.class, bb);
			long p1 = Struct.getPointer(mapped[0]);
			long p2 = Struct.getPointer(mapped[1]);
			if (p2 - p1 != 12)
				throw new IllegalStateException();
		}
	}

	public static class TestInterleavedMapping {
		public static void test() {
			int alignMargin = 4 - 1;
			int sizeof = 3 << 2;
			int count = 10;
			ByteBuffer bb = ByteBuffer.allocateDirect(count * sizeof + alignMargin);
			StructMemory.alignBuffer(bb, StructMemory.JVMWORD_ALIGNMENT);
			Vec3[] mapped1 = Struct.map(Vec3.class, bb, 24, 0);
			Vec3[] mapped2 = Struct.map(Vec3.class, bb, 24, 12);
			{
				long p1 = Struct.getPointer(mapped1[0]);
				long p2 = Struct.getPointer(mapped1[1]);
				if (p2 - p1 != 24)
					throw new IllegalStateException();
			}
			{
				long p1 = Struct.getPointer(mapped2[0]);
				long p2 = Struct.getPointer(mapped2[1]);
				if (p2 - p1 != 24)
					throw new IllegalStateException();
			}
			{
				long p1 = Struct.getPointer(mapped1[0]);
				long p2 = Struct.getPointer(mapped2[0]);
				if (p2 - p1 != 12)
					throw new IllegalStateException();
			}
		}
	}

	public static class TestOneInstance {
		public static void test() {
			new Vec3();
		}
	}

	public static class TestOneInstanceNull {
		@SuppressWarnings("all")
		public static void test() {
			Object obj = new Object();
			Vec3 vec = new Vec3();
			assert (vec != null);
			// assert (obj != null);
			// assert !(vec == null);
			// assert !(obj == null);
			// assert (vec == vec);
			// assert (obj == obj);
		}
	}

	public static class TestOneInstanceNullRef {
		public static void test() {
			// Vec3 vec = new Vec3();
			// vec.x = 5.6f;
			// echo(vec.x);
			// vec = null; FIXME: very hard to fix
			// echo(vec.x);
		}
	}

	public static class TestOneInstanceInstanceof {
		public static void test() {
			Object obj = new Object();
			Vec3 vec = new Vec3();
			assert !(vec instanceof Vec3);
			assert (obj instanceof Object);
		}
	}

	public static class TestTwoInstances {
		public static void test() {
			Vec3 vec1 = new Vec3();
			Vec3 vec2 = new Vec3();
			assert vec1 != vec2;
		}
	}

	public static class TestFields {
		public static void test() {
			Vec3 vec = new Vec3();
			vec.x = 13.34f;
			vec.y = 14.46f;
			vec.z = 15.56f;
			assert vec.x == 13.34f;
			assert vec.y == 14.46f;
			assert vec.z == 15.56f;
		}
	}

	public static class TestSum {
		public static void test() {
			Vec3 vec1 = new Vec3();
			vec1.x = 13.34f;
			vec1.y = 14.46f;
			vec1.z = 15.58f;

			Vec3 vec2 = new Vec3();
			vec2.x = 1 + vec1.x;
			vec2.y = 2 + vec1.y;
			vec2.z = 3 + vec1.z;

			assert vec2.x == 14.34f;
			assert vec2.y == 16.46f;
			assert vec2.z == 18.58f;
		}
	}

	public static class TestSetter {
		public static void test() {
			Vec3 v = new Vec3();
			assert v.x == 0;
			assert v.y == 0;
			assert v.z == 0;
			Vec3 ref = v.set(4567.8f, 4.5f, 3);
			if (v != ref)
				throw new IllegalStateException();
			assert ref.x == 4567.8f;
			assert ref.y == 4.5f;
			assert ref.z == 3f;
		}
	}

	public static class TestMethodPass {
		public static void test() {
			Vec3 vec = new Vec3();
			vec.x = 3.34f;
			vec.y = 4.46f;
			vec.z = 5.56f;

			test(vec);
		}

		public static void test(Vec3 vec) {
			assert vec.x == 3.34f;
			assert vec.y == 4.46f;
			assert vec.z == 5.56f;
		}
	}

	public static class TestIfFlow {
		public static void test() {
			Vec3 vec = new Vec3();

			if (Math.random() < 0.5)
				return;
			echo(vec.x);
		}

		public static void test2() {
			Vec3 vec = new Vec3();

			if (Math.random() < 0.5)
				echo(vec.x);
			return;
		}

		public static void test3() {
			Vec3 vec = new Vec3();

			if (Math.random() < 0.5)
				echo(vec.x);
			else
				echo(vec.x);
		}
	}

	public static class TestLoopFlow {
		public static void test() {
			Vec3 vec = new Vec3();
			for (int i = 0; i < 3; i++) {
				echo(vec.x);
			}
			echo(vec.x);
		}

		public static void test2() {
			Vec3 vec = new Vec3();
			while (Math.random() < 0.5) {
				echo(vec.x);
			}
			echo(vec.x);
		}

		public static void test3() {
			Vec3 vec = new Vec3();
			do {
				echo(vec.x);
			} while (Math.random() < 0.5);
			echo(vec.x);
		}

		public static void test4() {
			for (int i = 0; i < 3; i++) {
				echo(new Vec3().x);
			}
		}

		public static void test5() {
			while (Math.random() < 0.5) {
				echo(new Vec3().x);
			}
		}

		public static void test6() {
			do {
				echo(new Vec3().x);
			} while (Math.random() < 0.5);
		}
	}

	public static class TestConstructor {
		public static void test() {
			Vec3 v;

			v = new Vec3();
			assert v.x == 0.0f;
			assert v.y == 0.0f;
			assert v.z == 0.0f;

			v = new Vec3(1, 2, 3);
			assert v.x == 1;
			assert v.y == 2;
			assert v.z == 3;

			v = new Vec3(1.2f, 3.4f, 5.6f);
			assert v.x == 1.2f;
			assert v.y == 3.4f;
			assert v.z == 5.6f;

			v = new Vec3(1337f);
			assert v.x == 1337f;
			assert v.y == 1337f;
			assert v.z == 1337f;
		}
	}

	public static class TestTryFinally {
		public static void test() {
			Vec3 a = new Vec3();

			try {
				a.x = 5;
			} finally {
				a.y = 6;
			}
		}
	}

	public static class TestTryCatchFinally {
		public static void test() {
			Vec3 a = new Vec3();

			try {
				a.x = 13;
			} catch (Throwable t) {

			} finally {
				a.y = 14;
			}

			try {
				a.x = 13;
			} catch (Throwable t) {
				System.out.println(t);
			} finally {
				a.y = 14;
			}

			try {
				a.x = 13;
			} catch (Throwable t) {
				throw t;
			} finally {
				a.y = 14;
			}

			try {
				a.x = 13;
			} catch (Throwable t) {
				System.out.println(t);
				throw new IllegalStateException("doh!");
			} finally {
				a.y = 14;
			}
		}
	}

	public static class TestInstanceMethod {
		public static void test() {
			Vec3 a = new Vec3();
			a.x = 5;

			Vec3 b = new Vec3();
			b.x = 6;

			Vec3 vec = new Vec3();
			vec.add(a);
			vec.add(b);

			echo(vec.x);

			Vec3 copy = vec.copy();
			echo(copy.x);

			copy.x = 15;
			echo(copy.x);
			echo(vec.x);
		}
	}

	public static class TestStructReturnType {
		public static void test() {
			Vec3 vec1 = new Vec3();
			vec1.x = 13.37f;
			Vec3 vec2 = returnSelf(vec1);
			Vec3 vec3 = returnCopy(vec1);

			if (vec1 != vec2)
				throw new IllegalStateException("vec1 != vec2");
			if (vec1 == vec3)
				throw new IllegalStateException("vec1 == vec3");

			vec1.x = 73.31f;
		}

		@TakeStruct
		public static Vec3 returnSelf(Vec3 vec) {
			return vec;
		}

		@CopyStruct
		public static Vec3 returnCopy(Vec3 vec) {
			return vec;
		}
	}

	public static class TestStack {
		public static void test() {
			StructAllocationStack sas = StructThreadLocalStack.getStack();

			Vec3 vec = new Vec3();
			vec.x = 1;
			System.out.println("sas.sa=" + sas.isInvalid(Struct.getPointer(vec)));

			vec = self(vec);
			System.out.println("sas.se=" + sas.isInvalid(Struct.getPointer(vec)));
			vec.x = 3;
			if (vec.y != 13)
				throw new IllegalStateException();

			vec = copy();
			vec.x = 4;
			System.out.println("sas.co=" + sas.isInvalid(Struct.getPointer(vec)));
			if (vec.y != 14)
				throw new IllegalStateException();

			vec = pass();
			System.out.println("sas.pa=" + sas.isInvalid(Struct.getPointer(vec)));

			System.out.println(sas.isOnBlock(Struct.getPointer(vec)));
			vec.x = 5; // must crash, as the struct is on the part of the stack
						// that was popped
			System.out.println("oo");
			if (vec.y != 15)
				throw new IllegalStateException();
		}

		@TakeStruct
		private static Vec3 self(Vec3 v) {
			v.y = 13;
			return v;
		}

		@CopyStruct
		private static Vec3 copy() {
			Vec3 v = new Vec3();
			v.y = 14;
			return v;
		}

		@TakeStruct
		private static Vec3 pass() {
			Vec3 v = new Vec3();
			v.y = 15;
			return v;
		}
	}

	public static class TestArray {
		public static void test() {
			Vec3[] arr = new Vec3[10];
			arr[arr.length - 1].x = 4.5f;
			arr[arr.length - 1].set(5.6f, 6.7f, 7.8f);

			long p1 = Struct.getPointer(arr[0]);
			long p2 = Struct.getPointer(arr[1]);
			if (p2 - p1 != 12)
				throw new IllegalStateException();

			Vec3 a = arr[0];
			Vec3 b = arr[1];

			a.x = 3.7f;
			b.x = 4.8f;
			assert a.x == arr[0].x;
			assert b.x == arr[1].x;
		}
	}

	// ----------------

	public static class TestPerformance {
		public static void test() {

			Random rndm = new Random(12153);
			NormalVec3 nv = new NormalVec3();
			Vec3 sv = new Vec3();

			NormalVec3[] arr2 = new NormalVec3[1024];
			Vec3[] arr3 = new Vec3[1024];
			Vec3[] arr4 = new Vec3[1024];

			for (int k = 0; k < 16; k++) {
				System.out.println();
				float p = 0;
				sv.x = nv.x = rndm.nextFloat();
				sv.y = nv.y = rndm.nextFloat();
				sv.z = nv.z = rndm.nextFloat();

				long[] tA = new long[32];
				long[] tB = new long[32];

				// ---

				for (int i = 0; i < tA.length; i++) {
					long t0 = System.nanoTime();
					benchInstanceNew(arr2);
					long t1 = System.nanoTime();
					tA[i] = t1 - t0;
				}

				for (int i = 0; i < tB.length; i++) {
					long t0 = System.nanoTime();
					benchStructNew(arr3);
					long t1 = System.nanoTime();
					tB[i] = t1 - t0;
				}

				System.out.println("instance creation:       " + tA[tA.length / 2] / 1000 + "us [" + p + "]");
				System.out.println("struct creation:         " + tB[tB.length / 2] / 1000 + "us [" + p + "]");

				// ---

				for (int i = 0; i < tA.length; i++) {
					long t0 = System.nanoTime();
					p += benchInstanceAccess(nv);
					long t1 = System.nanoTime();
					tA[i] = t1 - t0;
				}

				for (int i = 0; i < tB.length; i++) {
					long t0 = System.nanoTime();
					p += benchStructAccess(sv);
					long t1 = System.nanoTime();
					tB[i] = t1 - t0;
				}

				System.out.println("instances access:        " + tA[tA.length / 2] / 1000 + "us [" + p + "]");
				System.out.println("struct access:           " + tB[tB.length / 2] / 1000 + "us [" + p + "]");

				// ---

				for (int i = 0; i < tA.length; i++) {
					long t0 = System.nanoTime();
					p += benchInstanceArray(arr2);
					long t1 = System.nanoTime();
					tA[i] = t1 - t0;
				}

				for (int i = 0; i < tB.length; i++) {
					long t0 = System.nanoTime();
					p += benchStructArray(arr4);
					long t1 = System.nanoTime();
					tB[i] = t1 - t0;
				}

				System.out.println("instance array access:   " + tA[tA.length / 2] / 1000 + "us [" + p + "]");
				System.out.println("struct array access:     " + tB[tB.length / 2] / 1000 + "us [" + p + "]");
			}
		}

		private static void benchInstanceNew(NormalVec3[] arr) {
			for (int i = 0; i < 128; i++) {
				benchInstanceNew2(arr);
			}
		}

		private static void benchInstanceNew2(NormalVec3[] arr) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = new NormalVec3(1, 2, 3);
			}
		}

		private static void benchStructNew(Vec3[] arr) {
			for (int i = 0; i < 128; i++) {
				benchStructNew2(arr);
			}
		}

		private static void benchStructNew2(Vec3[] arr) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = new Vec3(1, 2, 3);
			}
		}

		private static float benchInstanceAccess(NormalVec3 nv) {
			float p = 0;
			for (int i = 0; i < 1024 * 1024; i++) {
				p += nv.x * nv.y + nv.z;
				p *= nv.y * nv.z + nv.x;
				p -= nv.z * nv.x + nv.y;
			}
			return p;
		}

		private static float benchStructAccess(Vec3 nv) {
			float p = 0;
			for (int i = 0; i < 1024 * 1024; i++) {
				p += nv.x * nv.y + nv.z;
				p *= nv.y * nv.z + nv.x;
				p -= nv.z * nv.x + nv.y;
			}
			return p;
		}

		private static float benchInstanceArray(NormalVec3[] arr) {
			float p = 0;
			for (int k = 0; k < 64; k++) {
				for (int i = 0, len = arr.length - 2; i < len; i++) {
					p += arr[i + 0].x * arr[i + 0].y + arr[i + 0].z;
					p *= arr[i + 1].y * arr[i + 1].z + arr[i + 1].x;
					p -= arr[i + 2].z * arr[i + 2].x + arr[i + 2].y;
				}
			}
			return p;
		}

		private static float benchStructArray(Vec3[] arr) {
			float p = 0;
			for (int k = 0; k < 64; k++) {
				for (int i = 0, len = arr.length - 2; i < len; i++) {
					p += arr[i + 0].x * arr[i + 0].y + arr[i + 0].z;
					p *= arr[i + 1].y * arr[i + 1].z + arr[i + 1].x;
					p -= arr[i + 2].z * arr[i + 2].x + arr[i + 2].y;
				}
			}
			return p;
		}
	}

	// ----------------

	public static void echo(String v) {
		// System.out.println(v);
	}

	public static void echo(float v) {
		// System.out.println(v);
	}

	public static void echo(boolean v) {
		// System.out.println(v);
	}

	@StructType(disableClearMemory = true)
	public static class Vec3 {
		@StructField
		public float x;
		@StructField
		public float y;
		@StructField
		public float z;

		public Vec3() {
			this(0.0f, 0.0f, 0.0f);
		}

		public Vec3(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Vec3(float xyz) {
			this(xyz, xyz, xyz);
		}

		@TakeStruct
		public Vec3 add(Vec3 that) {
			this.x += that.x;
			this.y += that.y;
			this.z += that.z;
			return this;
		}

		@TakeStruct
		public Vec3 mul(Vec3 that) {
			this.x *= that.x;
			this.y *= that.y;
			this.z *= that.z;
			return this;
		}

		@TakeStruct
		public Vec3 set(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		@CopyStruct
		public Vec3 copy() {
			return this;
		}

		@Override
		public String toString() {
			return "Vec3[" + x + ", " + y + ", " + z + "]";
		}

		public static int aaaaaaah;

		public static void noob() {
			// System.out.println("n00b!");
		}
	}

	@StructType
	public static class Ship {
		private static int id_gen = 100000;
		@StructField
		public int id;
		@StructField
		public Vec3 pos;

		public Ship() {
			id = ++id_gen;// new Random().nextInt(); // FIXME
			// id = new Random().nextInt(); // FIXME
		}

		@Override
		public String toString() {
			return "Ship[id=" + id + ", pos=" + pos.toString() + "]";
		}
	}

	@StructType
	public static class PosVelRef {
		@StructField
		public int id;
		@StructField
		public Vec3 pos;
		@StructField
		public Vec3 vel;

		@Override
		public String toString() {
			return "PosVelRef[id=" + id + ", pos=" + pos.toString() + ", vel=" + vel.toString() + "]";
		}
	}

	@StructType(sizeof = 4 + 12 + 12)
	public static class PosVelView {
		@StructField
		public int id;

		@TakeStruct
		public Vec3 pos() {
			return Struct.view(this, Vec3.class, 4);
		}

		@TakeStruct
		public Vec3 vel() {
			return Struct.view(this, Vec3.class, 16);
		}

		@Override
		public String toString() {
			return "PosVelView[id=" + id + ", pos=" + pos().toString() + ", vel=" + vel().toString() + "]";
		}
	}

	@StructType
	public static class StructEmbed {
		@StructField(embed = true)
		public Vec3 pos;
		@StructField(embed = true)
		public Vec3 vel;

		@Override
		public String toString() {
			return "StructEmbed[]";
		}
	}
}
