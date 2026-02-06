import Globals
from Globals import *
import pygame
import os
grenade_image = pygame.image.load("images/asset/grenade.png")
class grenade(pygame.sprite.Sprite):
    def __init__(self, x, y,direction):
            pygame.sprite.Sprite.__init__(self)
            self.fuse_timer = 100
            self.fuse_start_time = -1
            self.explosion_duration = 500
            self.explosion_start_time = -1
            self.explosion_started = False
            self.explosion_ended = False
            self.explosion_images = []
            self.thrown_time = pygame.time.get_ticks()
            self.explosion_index = 0
            self.x = x
            self.y = y
            self.speed_y = -12
            self.speed_x = 12
            self.direction = direction
            self.rect = grenade_image.get_rect()
            self.last_updated = 0 #last time of explosion updated
            self.animation_len = len(os.listdir('images/explosion'))
            for i in range(self.animation_len):
                self.explosion_images.append(pygame.image.load(f'images/explosion/{i}.png'))
    def update(self,sol,screen_name): # screen_name for the call of explote()
        # if self.speed_x == 0 and pygame.time.get_ticks() - self.fuse_start_time > self.fuse_timer and self.explosion_start_time < 0:
        if self.speed_x == 0 :
            self.explosion_started = True
            #print(f"here started {pygame.time.get_ticks()}")
            self.explosion_start_time = pygame.time.get_ticks()
        # if self.explosion_started and pygame.time.get_ticks() - self.explosion_start_time > self.explosion_duration:
        if self.explosion_started and pygame.time.get_ticks() - self.explosion_start_time > self.explosion_duration:
            self.explosion_ended = True
        # kill the soldier
        if self.explosion_started and pygame.time.get_ticks() - self.explosion_start_time <= self.explosion_duration:
            if pygame.sprite.spritecollide(sol, pygame.sprite.Group([self]), False):
                if sol.alive:
                    sol.alive = False
                    sol.update_action(3)
                    # print('DEAD DEAD')
                    # change other variables of soldier
        if self in grenade_arr and self.explosion_ended:
            grenade_arr.remove(self)
            #print(f"removed at index = {self.explosion_index}")
        # explosion
        #self.explote(screen_name)
        if self in grenade_arr and self.explosion_started and self.explosion_index < self.animation_len:
            #print("here")
            self.explote(screen_name)
    def explote(self,screen_name):
        if pygame.time.get_ticks() - self.last_updated > 100:
            #print(f'exploted_index = {self.explosion_index} animation len {self.animation_len}')
            self.last_updated = pygame.time.get_ticks()
            img = self.explosion_images[self.explosion_index]
            rect = img.get_rect()
            rect.center = (self.x,self.y)
            rect.x += Globals.screen_scroll
            screen_name.blit(img,rect)
            self.explosion_index += 1


    def draw_grenade(self,screen_name):
        grenade_width = int(grenade_image.get_width())
        grenade_height = int(grenade_image.get_height())
        scale = 1
        scaled_image = pygame.transform.scale(grenade_image,(grenade_width*scale,grenade_height*scale))
        self.rect = scaled_image.get_rect()
        self.rect.center = (self.x,self.y)
        if not self.explosion_started:
            screen_name.blit(scaled_image,self.rect)
        #print(f'drawn = {self.y + self.rect.size[1]}')
        #print("drawn")
    def move(self):
        if self.speed_x == 0:
            return
        dx = self.speed_x * self.direction
        dy = self.speed_y
        if self.rect.left + dx > screen_width or self.rect.right+dx < 0:
            self.direction *= -1
            dx = self.speed_x * self.direction
        self.x += dx + Globals.screen_scroll
        self.y += dy
        self.speed_y += gravity*2

        for obs in obstacle_list:
            # if pygame.sprite.collide_rect(self.rect,obs[1]):
            if self.rect.colliderect(obs[1]):
                self.speed_x = 0
                # self.y = obs[1].top - self.rect.height
                self.y -= 1.5 * dy
                self.speed_y = 0
