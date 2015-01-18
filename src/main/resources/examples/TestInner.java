public class TestInner
{
	public TestInner()
	{
		System.out.println("TestInner");
		new Inner1();
	}

	class Inner1
	{
		public Inner1()
		{
			System.out.println("Inner1");
			new Inner2();
		}

		class Inner2
		{
			public Inner2()
			{
				System.out.println("Inner2");
			}
		}
	}

	public static void main(String[] args)
	{
		new TestInner();
	}
}