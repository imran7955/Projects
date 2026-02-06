from Globals import *
from Environment import *
from Bullet import *
from Item import *
from Soldier import *
import Button
import pygame


pygame.init()
# screen = pygame.display.set_mode((screen_width,screen_height))
pygame.display.set_caption('Shooter')
# f1 = soldier(250,400,1.8,'fighter')
RED = (255, 0, 0)
WHITE = (255,255,255)
GREEN = (0,255,5)
SCORE_COLOR = (0,0,0)

enemy_group = []
# f2 = soldier(600,400,1.8,'enemy')
# f3 = soldier(300,400,1.8,'enemy')
# enemy_group.append(f2)
# enemy_group.append(f3)

# ----------------- ITEM BOXES TEMP ------

# itm1 = item_box('health',800,line_level)
# item_box_group.add(itm1)
# itm2 = item_box('bullet',600,line_level)
# item_box_group.add(itm2)
# itm3 = item_box('grenade',500,line_level)
# item_box_group.add(itm3)
# ----------------- ITEM BOXES TEMP ------
print(env_grid)
class environment():
    def __init__(self):
        pass
        # self.obstacle_list = []
    def make_environment(self,grid):

        for y, row in enumerate(grid):
            for x, tile in enumerate(row):
                tile = int(tile)
                if tile >= 0:
                    img = tile_image_list[tile]
                    img_rect =img.get_rect()
                    img_rect.x = x * tile_size
                    img_rect.y = y * tile_size
                    if tile <= 8:
                        obstacle_list.append((img,img_rect))
                    elif tile >= 9 and tile <= 10:
                        ww = water(img, x * tile_size, y * tile_size)
                        water_group.add(ww)
                    elif tile >= 11 and tile <= 14:
                        dd = decoration(img,x * tile_size , y * tile_size)
                        decoration_group.add(dd)
                    elif tile == 15:
                        f1 = soldier(x * tile_size,y * tile_size,1.8,'fighter')
                    elif tile == 16:
                        enemy = soldier(x * tile_size,y * tile_size,1.8,'enemy')
                        enemy_group.append(enemy)
                    elif tile == 17:
                        itm = item_box('bullet', x * tile_size,y * tile_size)
                        item_box_group.add(itm)
                    elif tile == 18:
                        print(f'Grenade created at {y},{x}')
                        itm = item_box('grenade', x * tile_size,y * tile_size)
                        item_box_group.add(itm)
                    elif tile == 19:
                        itm = item_box('health', x * tile_size, y * tile_size)
                        item_box_group.add(itm)
                    elif tile == 20:
                        ee = exit(img, x * tile_size, y * tile_size)
                        exit_group.add(ee)
        return f1
    def draw(self):
        for tile in obstacle_list:
            tile[1][0] += screen_scroll
            screen.blit(tile[0],tile[1])
class decoration(pygame.sprite.Sprite):
    def __init__(self,img,x,y):
        pygame.sprite.Sprite.__init__(self)
        self.image = img
        self.rect = self.image.get_rect()
        self.rect.midtop = (x + tile_size // 2, y + (tile_size - self.image.get_height()))
    def update(self):
        self.rect.x += screen_scroll
class water(pygame.sprite.Sprite):
    def __init__(self,img,x,y):
        pygame.sprite.Sprite.__init__(self)
        self.image = img
        self.rect = self.image.get_rect()
        self.rect.midtop = (x + tile_size // 2, y + (tile_size - self.image.get_height()))
    def update(self):
        # pass
        self.rect.x += screen_scroll
class exit(pygame.sprite.Sprite):
    def __init__(self,img,x,y):
        pygame.sprite.Sprite.__init__(self)
        self.image = img
        self.rect = self.image.get_rect()
        self.rect.midtop = (x + tile_size // 2, y + (tile_size - self.image.get_height()))
    def update(self):
        self.rect.x += screen_scroll

def draw_bg():
    screen.fill((40, 40, 39))
    width = background.get_width()
    # width = 0
    for i in range(6):
        screen.blit(background,(i * width - bg_scroll * .3,0))
        screen.blit(tree1,(i * width - bg_scroll * .5,screen_height - tree1.get_height() ))
        screen.blit(tree2,(i * width - bg_scroll * .7,screen_height - tree1.get_height() ))
def reset_level():
    obstacle_list.clear()
    enemy_group.clear()
    bul_arr.clear()
    grenade_arr.clear()
    item_box_group.empty()
    decoration_group.empty()
    water_group.empty()
    exit_group.empty()

    temp_grid = []
    for i in range(rows):
        temp_grid.append([-1] * cols)
    return  temp_grid
env1 = environment()
f1 = env1.make_environment(env_grid)
# start_btn = Button.Button(screen_width // 2,screen_height // 2 -100,start_img,1)
# exit_btn = Button.Button(screen_width // 2,screen_height // 2 + 100,exit_img,1)
start_btn = Button.Button(screen_width // 2,100,start_img,1)
exit_btn = Button.Button(screen_width // 2,300,exit_img,1)
restart_btn = Button.Button(screen_width // 2,screen_height // 2,restart_img,2.5)
start_game = False
while running == True:
    ## start of the definition of game
    pygame.time.Clock().tick(FPS)
    if start_game == False:
        # screen.fill((40, 40, 39))
        screen.blit(control_info_img,(0,0))
        if start_btn.draw(screen):
            start_game = True
        if exit_btn.draw(screen):
            running = False
    else:
        draw_bg()
        env1.draw()
        for en in enemy_group:
            en.update()
            en.rect.x += screen_scroll
            en.draw_soldier(screen)
            if f1.alive and en.alive:
                en.auto_move(f1)
                # en.update_action(1)
        f1.update()
        f1.draw_soldier(screen)
        # ---------------- ITEM DRAW ----------
        item_box_group.update(f1)
        decoration_group.update()
        water_group.update()
        exit_group.update()
        decoration_group.draw(screen)
        water_group.draw(screen)
        exit_group.draw(screen)
        item_box_group.draw(screen)
        # ---------------- ITEM DRAW ----------
        draw_text(f'Health: {f1.health}',pygame.font.SysFont('Arial',20),SCORE_COLOR,5,5)
        pygame.draw.rect(screen,RED,(95,10,1.5 * f1.maximum_health,15))
        pygame.draw.rect(screen,GREEN,(95,10,1.5 * f1.health,15))
        draw_text(f'Bullet: {f1.bullet_health}',pygame.font.SysFont('Arial',20),SCORE_COLOR,5,25)
        draw_text(f'Grenade: {f1.grenade_health}',pygame.font.SysFont('Arial',20),SCORE_COLOR,5,45)
        if f1.alive:
            if shoot and (pygame.time.get_ticks() - f1.last_shooted) > 150 and f1.bullet_health > 0:
                # print(f"Diff = {pygame.time.get_ticks() - f1.last_shooted}")
                f1.shoot()
            if throw_grenade and (pygame.time.get_ticks() - f1.last_throwed > 200) and f1.grenade_health > 0:
                f1.throw_grenade()
            if moving_left or moving_right:
                f1.update_action(1)
            elif f1.jumping:
                f1.update_action(2)
            else:
                f1.update_action(0)
            screen_scroll, level_complete = f1.move(moving_left,moving_right,moving_up,moving_down)
            Globals.screen_scroll = screen_scroll # don't know why screen_scroll is acting as a variable of main.py
            bg_scroll -= screen_scroll
            fighter_rect = f1.rect
            # Reason of drawing by another loop after updating: after updating instances may get removed
            for bul in bul_arr:
                bul.move()
                # bul.update(f2)
                # bul.update(f3)
                for en in enemy_group:
                    bul.update(en)
                    # bul.update(f1) # problem: update is used so that a bullet doesn't go through an alive soldier
            for bul in bul_arr:
                bul.draw_bullet(screen)
            for gre in grenade_arr:
                gre.move()
                for en in enemy_group:
                    gre.update(en,screen)
                    gre.update(en,screen)
            for gre in grenade_arr:
                gre.draw_grenade(screen)

            if level_complete:
                level += 1
                bg_scroll = 0
                env_grid = reset_level()
                if level < Max_level:
                    with open(f'level{level}_data.csv', newline='') as file:
                        grid = csv.reader(file, delimiter=',')
                        for i, lines in enumerate(grid):
                            for j, tile in enumerate(lines):
                                env_grid[i][j] = tile
                        env1 = environment()
                        f1 = env1.make_environment(env_grid)
        else:
            screen_scroll = 0
            if restart_btn.draw(screen):
                bg_scroll = 0
                env_grid.clear()
                env_grid = reset_level()
                with open(f'level{level}_data.csv', newline='') as file:
                    grid = csv.reader(file, delimiter=',')
                    for i, lines in enumerate(grid):
                        for j, tile in enumerate(lines):
                            env_grid[i][j] = tile
                    env1 = environment()
                    f1 = env1.make_environment(env_grid)

    for ev in pygame.event.get():
        if ev.type == pygame.QUIT:
            running = False
        if ev.type == pygame.KEYDOWN:
            if ev.key == pygame.K_LEFT:
                moving_left = True
            if ev.key == pygame.K_RIGHT:
                moving_right = True
            if ev.key == pygame.K_UP:
                moving_up = True
                #gravity = 0
            if ev.key == pygame.K_DOWN:
                moving_down = True
            if ev.key == pygame.K_w:
                f1.jumping = True
            if ev.key == pygame.K_SPACE:
                shoot = True
            if ev.key == pygame.K_q:
                throw_grenade = True
        if ev.type == pygame.KEYUP:
            if ev.key == pygame.K_LEFT:
                moving_left = False
            if ev.key == pygame.K_RIGHT:
                moving_right = False
            if ev.key == pygame.K_UP:
                moving_up = False
                #gravity = 8
            if ev.key == pygame.K_DOWN:
                moving_down = False
            if ev.key == pygame.K_SPACE:
                shoot = False
            if ev.key == pygame.K_q:
                throw_grenade = False
    pygame.display.update()
    ## end of the definition of game
pygame.quit()
