import os

# for x in range(1,41):
# 	if x < 10:
# 		path = "cards-0" + str(x)+".png"
# 		f = open(path)
# 	else:
# 		path = "cards-" + str(x)+".png"
# 		f = open(path)
# 	
# 	data = f.read()
# 	f.close()
# 	os.remove(path)
# 	
# 	f = open("cards"+str(x)+".png", "w+")
# 	f.write(data)
# 	f.close()

y = 1
for x in range(42, 52):
	path = "cards-" + str(x)+".png"
	f = open(path)
	data = f.read()
	
	f.close()
	os.remove(path)
	
	f = open("cell"+str(y)+".png", "w+")
	f.write(data)
	f.close()
	
	y += 1

y = 1
for x in range(52, 55):
	path = "cards-" + str(x)+".png"
	f = open(path)
	data = f.read()
	
	f.close()
	os.remove(path)
	
	f = open("opencard"+str(y)+".png", "w+")
	f.write(data)
	f.close()
	
	y += 1
	
		
		
