import pygame
# import Soldier
import csv
screen_width = 960
screen_height = int(screen_width * 0.7)
screen = pygame.display.set_mode((screen_width,screen_height))
running = True
line_level = 400
moving_speed = 20
gravity = 1
moving_left = False
moving_right = False
moving_up = False
moving_down = False
shoot = False
throw_grenade = False
FPS = 60
# bul_arr = pygame.sprite.Group()

# f1 = Soldier.soldier(400,400,2,'fighter')
# f5 = Soldier.soldier(600,350,2,'enemy')
# f1 = soldier(400,400,1.8,'fighter')
'''--------------- --------------
in soldier class, we need the fighter rect to sense and shoot by the enemy.
to do so, we declare a rect here and we will always update it in the main program and use it 
in the soldier class
-------------- --------------'''
fighter_rect = pygame.Rect(0,0,0,0)

level = 1
rows = 16
cols = 150
tile_size = screen_height // rows
tile_types = 21
def draw_text(text,font,color,x,y):
    img = font.render(text,True,color)
    screen.blit(img,(x,y))

env_grid = []
for i in range(rows):
    env_grid.append([-1] * cols)
with open(f'level{level}_data.csv', newline='') as file:
    grid = csv.reader(file, delimiter=',')
    for i,lines in enumerate(grid):
        for j,tile in enumerate(lines):
            env_grid[i][j] = tile
tile_image_list = []
for i in range(tile_types):
    img = pygame.image.load(f'images/tiles/{i}.png')
    img = pygame.transform.scale(img,(tile_size,tile_size))
    tile_image_list.append(img)

bul_arr = []
grenade_arr = []
# item_box_group = pygame.sprite.Group()
enemy_group = []
obstacle_list = []
item_box_group = pygame.sprite.Group()
decoration_group = pygame.sprite.Group()
water_group = pygame.sprite.Group()
exit_group = pygame.sprite.Group()

tree1 = pygame.image.load('images/background/tree1.png')
tree2 = pygame.image.load('images/background/tree2.png')
# hill = pygame.image.load('images/background/hill.png')
# sky = pygame.image.load('images/background/sky.png')
background = pygame.image.load('images/background/background3.jpg')

scroll_margin = 200
screen_scroll = 0
scroll_screen_global = 0
bg_scroll = 0
level_len = 0

level_len = len(env_grid[0])

start_img = pygame.image.load('images/start.png')
exit_img = pygame.image.load('images/exit.png')
restart_img = pygame.image.load('images/restart.png')
control_info_img = pygame.image.load('images/control_info.jpg')

Max_level = 3


