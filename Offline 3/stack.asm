addi $t0, $zero, 105    

subi $t1, $t0, 5      

add $t3, $t0, $t1     

sll $t0, $t3, 15

lw $t1, 5($t0)

push $t1
sw $t1, 0($sp)
subi $sp,$sp,1

pop $t1
lw $t1, 0($sp)
addi $sp,$sp,1

push 3($t0)
lw $t5, 3($t0)
sw $t5, 0($sp)
subi $sp,$sp,1
