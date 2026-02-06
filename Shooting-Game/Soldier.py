import random

import Globals
from Globals import *
from Grenade import *
from Bullet import *
import pygame
import os
class soldier(pygame.sprite.Sprite):
    def __init__(self, x, y,scale,type):
        pygame.sprite.Sprite.__init__(self)
        self.scale = scale
        self.alive = True
        self.flip = False
        self.direction = 1 # 1 means: facing right side
        self.updated = pygame.time.get_ticks()
        self.lst_animation = []
        self.type = type
        self.state = ['idle','run','jump','death']
        self.action = 0 if self.type == 'fighter' else 1
        self.index = 0
        self.jumping = False
        self.levitating = False
        self.y_velocity = 0
        self.health = 70
        self.maximum_health = 100
        self.bullet_health = 100
        self.maximum_bullet_health = 100
        self.grenade_health = 30
        self.last_shooted = 0
        self.last_throwed = 0

        # for enemy (AutoMove)
        self.move_counter = 0
        self.is_idle = False
        self.idle_timer = 100
        self.sense_box = pygame.Rect(0,0,120,30)
        for work in self.state:
            animation_len = len(os.listdir(f'images/{type}/{work}'))
            lst_temp = []
            for i in range(animation_len):
                temp_img = pygame.image.load(f'images/{type}/{work}/{i}.png')
                new_w = int(temp_img.get_width() * scale)
                new_h = int(temp_img.get_height() * scale)
                lst_temp.append( pygame.transform.scale(temp_img,(new_w,new_h)))
            self.lst_animation.append(lst_temp)
        self.image = self.lst_animation[self.action][self.index]
        self.rect = self.image.get_rect()
        self.rect.center = (x, y)
    def update(self):
        self.animate_soldier()
        self.check_if_alive()
        if pygame.sprite.spritecollide(self,pygame.sprite.Group(bul_arr),False):
            self.health -= 3 if self.type == 'fighter' else 40

            #print(self.health)

    def check_if_alive(self):
        if self.health <= 0:
            self.health = 0
            self.update_action(3)
            self.alive = False

    def update_action(self, updated_action):
        if updated_action != self.action:
            self.action = updated_action
            self.updated = pygame.time.get_ticks()
            self.index = 0
    def animate_soldier(self):
        update_period = 100
        self.image = self.lst_animation[self.action][self.index]
        if pygame.time.get_ticks() - self.updated >= update_period:
            self.updated = pygame.time.get_ticks()
            self.index += 1
        if self.index == len(self.lst_animation[self.action]):
            if self.action == 3:
                self.index = len(self.lst_animation[self.action]) - 1
            else:
                self.index = 0
    def shoot(self):
        self.last_shooted = pygame.time.get_ticks()
        self.bullet_health -= 1
        x = self.rect.centerx + 0.6 * self.rect.size[0] * self.direction
        y = self.rect.centery + 0 * self.rect.size[1]
        new_bul = bullet(x, y, self.direction)
        bul_arr.append(new_bul)
        # print(self.bullet_health)
    def throw_grenade(self):
        self.grenade_health -= 1
        self.last_throwed = pygame.time.get_ticks()
        x = self.rect.centerx + (self.rect.size[0] / 2  * self.direction)
        y = self.rect.y
        new_gre = grenade(x,y,self.direction)
        grenade_arr.append(new_gre)

    def move(self,left,right,up,down):
        dx = 0; dy = 0; screen_displacement = 0
        if left:
            dx -= moving_speed 	/ (1.5 if self.type == 'enemy' else 1)
            self.flip = True
            self.direction = -1
        if right:
            dx += moving_speed / (1.5 if self.type == 'enemy' else 1)
            self.flip = False
            self.direction = 1
        if up:
            dy -= 2 * moving_speed
            # self.y_velocity -= moving_speed
        if down:
            dy += 2 * moving_speed
            # self.y_velocity += moving_speed

        if self.jumping and self.levitating == False:
            self.levitating = True
            self.jumping = False
            self.y_velocity = -0.7 *moving_speed
            #dy = min(10,dy)
        self.y_velocity += gravity
        # self.y_velocity = min(self.y_velocity, 15)
        dy += self.y_velocity
        # print(f'obstacle size  = {len(obstacle_list)}')
        for tile in obstacle_list:
            #print('cheaking')
            if tile[1].colliderect(self.rect.x + dx, self.rect.y, self.rect.width, self.rect.height):
                dx = 0
                # print("MOVEment srtpped")
                if self.type == 'enemy':
                    self.direction *= -1
                    self.move_counter = 0
            if tile[1].colliderect(self.rect.x, self.rect.y + dy, self.rect.width, self.rect.height):
                # heating a tile from the ground
                if dy < 0:
                    self.y_velocity = 0
                    dy = tile[1].bottom - self.rect.top
                    # print(f'Here dy = {dy}')
                elif dy >= 0:
                    # print(f'here dy = {dy}')
                    self.y_velocity = 0
                    dy = tile[1].top - self.rect.bottom
                    self.levitating = False
                # elif self.type == 'enemy':
                #     self.direction *= -1
            if tile[1].colliderect(self.rect.x + dx, self.rect.bottom, self.rect.width,self.rect.height):
                self.direction *= -1
                # dx = 0
                self.move_counter = 0
        # dy += self.y_velocity
        if self.rect.left + dx < 0 or self.rect.right + dx > screen_width:
            dx = 0

        # water collision
        if pygame.sprite.spritecollide(self,water_group,False):
            self.health = 0
        level_complete = False
        if pygame.sprite.spritecollide(self,exit_group,False):
            level_complete = True
        if self.rect.bottom > screen_height:
            self.health = 0
        self.rect.x += dx
        self.rect.y += dy #+ self.y_velocity
        # print(f'level_len {level_len}')
        if self.type == 'fighter':
            # if (self.rect.left < scroll_margin and bg_scroll > abs(dx)) or\
            #         (self.rect.right > screen_width - scroll_margin and bg_scroll < (level_len * tile_size) - screen_width):
            # if (((self.rect.right > screen_width - scroll_margin) and (bg_scroll < (level_len * tile_size) - screen_width)) or (self.rect.left < scroll_margin and bg_scroll > abs(dx))):
            if self.rect.left < scroll_margin or self.rect.right >= screen_width - scroll_margin:
                self.rect.x -= dx
                screen_displacement = -dx


        # else:
        #     if self.rect.x > screen_width - int(self.image.get_width()):
        #         self.rect.x = screen_width - int(self.image.get_width())
        #         if self.direction == 1:
        #             self.direction = -1
        #         else:
        #             self.direction = 1
        #     if self.rect.x < 0:
        #         self.rect.x = 0
        #         if self.direction == 1:
        #             self.direction = -1
        #         else:
        #             self.direction = 1
        #     self.sense_box.center = (self.rect.centerx + self.rect.size[0]/2 * self.scale * self.direction,self.rect.centery)
        if self.rect.y + dy < 0:
            self.rect.y = 0

        # if self.rect.y > line_level - int(self.image.get_height()):
        #     self.rect.y = line_level - int(self.image.get_height())
        #     self.levitating = False
        #     self.y_velocity = 0
        # self.rect.x += dx
        # self.rect.y += dy
        return screen_displacement,level_complete
    def auto_move(self,fighter):
        #pygame.draw.rect(screen,(250,0,0),self.sense_box,2)
        # pygame.draw.rect(screen,(250,0,0),fighter_rect,2)
        if self.is_idle == False and random.randint(1,200) == 15:
            self.is_idle = True
            # print(f'before action = {self.action}')
            self.update_action(0)
            # print(f'after acttion = {self.action}')
            self.idle_timer = 100
        temp_rect = self.sense_box
        temp_arr = [fighter_rect]
        if self.sense_box.colliderect(fighter.rect):
            if pygame.time.get_ticks() - self.last_shooted > 500:
                self.update_action(0)
                self.shoot()
                self.last_shooted = pygame.time.get_ticks()
                # self.is_idle = True
                # print("HERE -------------- >>>>>>>>>>> ")
        else:
            if self.is_idle == False:
                if self.direction == 1:
                    self.move(False,True,False,False)
                else:
                    self.move(True,False, False, False)
                self.update_action(1)
                self.move_counter += 1
                self.sense_box.center = (self.rect.centerx + self.rect.size[0] / 2 * self.scale * self.direction, self.rect.centery)
            if self.move_counter >= tile_size: #line 162
                self.direction *= -1
                self.move_counter *= -1
                # print("here herre")
            else:
                self.idle_timer -= 1
                # print(f'idle timer = {self.idle_timer} self.action = {self.action}')
                if self.idle_timer <= 0:
                    # self.idle_timer = 0
                    self.is_idle = False
                    # self.update_action(1)
    def draw_soldier(self,screen_name):
        img = pygame.transform.flip(self.image,self.flip,False)
        screen_name.blit(img,self.rect)