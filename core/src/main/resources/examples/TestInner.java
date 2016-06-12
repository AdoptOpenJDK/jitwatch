public class TestInner
{
	public TestInner()
	{
		System.out.println("TestInner()");

		a();

		new Inner1();
	}

	public void a()
	{
		System.out.println("TestInner.a()");
	}

	class Inner1
	{
		public Inner1()
		{
			System.out.println("TestInner.Inner1()");

			b();

			new Inner2();
		}

		public void b()
		{
			System.out.println("TestInner.Inner1.b()");
		}

		class Inner2
		{
			public Inner2()
			{
				System.out.println("TestInner.Inner1.Inner2()");

				c();
			}

			public void c()
			{
				System.out.println("TestInner.Inner1.Inner2.c()");
			}

		}
	}

	public static void main(String[] args)
	{
		new TestInner();
	}
}
