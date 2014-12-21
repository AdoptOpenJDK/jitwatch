def add(x,y) {
   x+y
}

def y = 10000
def x = 0

while ( y-- > 0 ) {
    x = add(x, 1)
}

println x