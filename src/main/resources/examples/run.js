var sum = 0;

for (var i = 0; i < 1000000; i++)
{
    sum += addOne(sum);
    sum += addNinetyNine(sum);
}

function addOne(number)
{
    return add(number, 1);
}

function addNinetyNine(number)
{
    return add(number, 99);
}

function add(x, y)
{
    return x + y;
}