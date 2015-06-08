var sum = 0;

for (var i = 0; i < 1000000; i++)
{
    sum = addOne(sum);
    sum = addTwo(sum);
}

print(sum);

function addOne(number)
{
    return add(number, 1);
}

function addTwo(number)
{
    return add(number, 2);
}

function add(x, y)
{
    return x + y;
}
