# from Globals import *
# from Soldier import *
# from main import *
# class environment():
#     def __init__(self):
#         self.obstacle_list = []
#     def make_environment(self,grid):
#         for y, row in enumerate(grid):
#             for x, tile in enumerate(row):
#                 tile = int(tile)
#                 if tile >= 0:
#                     img = tile_image_list[tile]
#                     img_rect =img.get_rect()
#                     img_rect.x = x * tile_size
#                     img_rect.y = y * tile_size
#                     if tile <= 8:
#                         self.obstacle_list.append((img,img_rect))
#                     elif tile >= 9 and tile <= 10:
#                         pass #water
#                     elif tile >= 11 and tile <= 14:
#                         pass # decoration
#                     elif tile == 15:
#                         f1 = soldier(x * tile_size,y * tile_size,1.8,'fighter')
#                     elif tile == 16:
#                         enemy = soldier(x * tile_size,y * tile_size,1.8,'enemy')
#                         enemy_group.append(enemy)
#                     elif tile == 17:
#                         itm = item_box('bullet', x * tile_size,y * tile_size)
#                         item_box_group.add(itm)
#                     elif tile == 18:
#                         itm = item_box('grenade', x * tile_size,y * tile_size)
#                         item_box_group.add(itm)
#                     elif tile == 19:
#                         itm = item_box('health', x * tile_size, y * tile_size)
#                         item_box_group.add(itm)
#                     elif tile == 20:
#                         pass
#         return f1