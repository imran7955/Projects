import Globals
from Globals import *
import pygame
import os
class item_box(pygame.sprite.Sprite):
    def __init__(self,type,x,y):
        pygame.sprite.Sprite.__init__(self)
        # -- images ---
        img = pygame.image.load('images/asset/health_box.png')
        h = int(img.get_height() * .7)
        w = int(img.get_width() * .7)
        self.box_arr = {'health':pygame.transform.scale(pygame.image.load('images/asset/health_box.png'),(w,h)),
            'bullet':pygame.transform.scale(pygame.image.load('images/asset/bullet_box.png'),(w,h)),
            'grenade':pygame.transform.scale(pygame.image.load('images/asset/grenade_box.png'),(w,h))
        }
        # -- images ---
        self.box_type = type
        self.image = self.box_arr[type]
        self.rect = self.image.get_rect()
        self.rect.center = (x,y - self.image.get_height()/2)
    def update(self,soldier):
        self.rect.x += Globals.screen_scroll
        # print(f"pos {self.rect}")
        if pygame.sprite.collide_rect(self,soldier):
            print('colidedddddd')
            if self.box_type == 'health':
                soldier.health = min(soldier.maximum_health, soldier.health + 20)
            elif self.box_type == 'bullet':
                soldier.bullet_health = min(soldier.maximum_bullet_health, soldier.bullet_health + 20)
            else:
                soldier.grenade_health += 5
            self.kill()