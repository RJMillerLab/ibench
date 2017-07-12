f400 = open('values-400.csv', 'w')
f200 = open('values-200.csv', 'w')
f100 = open('values-100.csv', 'w')
f50 = open('values-50.csv', 'w')

f400.write('T\n')
f200.write('T\n')
f100.write('T\n')
f50.write('T\n')


for i in range(400):
 f400.write(str(i) + '\n')
 f200.write(str(i % 200) + '\n')
 f100.write(str(i % 100) + '\n')
 f50.write(str(i % 50) + '\n')

f400.close()
f200.close()
f100.close()
f50.close()

