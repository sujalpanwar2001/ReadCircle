import { Component, OnDestroy, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { KeycloakService } from 'src/app/services/keycloak/keycloak.service';
import * as SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { Notification } from './notification';


@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit, OnDestroy {

  loggedUser: string | any = ''

  socketClient: any = null;
  private notificationSubscription: any;
  unreadNotificationsCount = 0;
  notifications: Array<Notification> = [];

  constructor(
    private keycloakService: KeycloakService,
    private toastService: ToastrService
  ) {
  }

  ngOnInit(): void {
    this.navigationHandler();

    if (this.keycloakService.keycloak.tokenParsed?.sub) {
      // let ws = new SockJS('http://localhost:8088/api/v1/ws');
      let ws = new SockJS('http://144.24.124.183:8088/api/v1/ws');


      this.socketClient = Stomp.over(ws);
      this.socketClient.connect({ 'Authorization': 'Bearer ' + this.keycloakService.keycloak.token }, () => {
        this.notificationSubscription = this.socketClient.subscribe(
          `/user/${this.keycloakService.keycloak.tokenParsed?.sub}/notifications`,
          (message: any) => {
            console.log('receiving notification' + message);
            const notification = JSON.parse(message.body);
            if (notification) {
              this.notifications.unshift(notification);
              switch (notification.status) {
                case 'BORROWED':
                  this.toastService.info(notification.message, notification.bookTitle);
                  break;
                case 'RETURNED':
                  this.toastService.warning(notification.message, notification.bookTitle);
                  break;
                case 'RETURN_APPROVED':
                  this.toastService.success(notification.message, notification.bookTitle);
                  break;
              }
              this.unreadNotificationsCount++;
            }


          }, () => {
            console.error('Error while connecting to webSocket');
          });
      }
      );
    }
  }

  ngOnDestroy() {
    if (this.socketClient !== null) {
      this.socketClient.disconnect();
      this.notificationSubscription.unsubscribe();
      this.socketClient = null;
    }
  }

  private navigationHandler() {
    const linkColor = document.querySelectorAll('.nav-link');
    linkColor.forEach(link => {
      if (window.location.href.endsWith(link.getAttribute('href') || '')) {
        link.classList.add('active');
      }
      link.addEventListener('click', () => {
        linkColor.forEach(l => l.classList.remove('active'));
        link.classList.add('active');
      });
    });
  }

  async logout() {
    await this.keycloakService.logout();
  }

  get username() {
    // @ts-ignore
    return this.keycloakService.keycloak.tokenParsed?.given_name
  }
}



