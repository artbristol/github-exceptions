/// <reference path="typings/angular2/angular2.d.ts" />

import {Component, View, bootstrap, NgFor, NgIf} from 'angular2/angular2';

enum CatchBlockType  {bad, rethrow, unknown}

class JsonResponse {

    catchBlockType:CatchBlockType;
    date:Date;
    commitUrl:URL;
    exceptionBlock:String;

}

@Component({
    selector: 'todo-list'
})
@View({
    template: `
        <ul>
          <li *ng-for="#todo of todos">
            {{ todo }}
          </li>
        </ul>

        <input #todotext (keyup)="doneTyping($event)">
        <button (click)="addTodo(todotext.value)">Add Todo</button>
        <button (click)="getMore()">Get Stuffs</button>
              `,
    directives: [NgFor, NgIf]
})


class TodoList {
    todos:Array<string>;

    constructor() {
        this.todos = ["Eat Breakfast", "Walk Dog", "Breathe"];
    }

    addTodo(todo:string) {
        this.todos.push(todo);
    }

    getMore() {
        $.ajax({
            //cache: false,
            ifModified: true,
            url: "/events.json",
            dataType: "json",
            success: function (data:JsonResponse[]) {

                if ($.isArray(data)) {
                    data.forEach(function (jsonResponse:JsonResponse) {
                        window.console.log("got a repsonse: " + jsonResponse.exceptionBlock);

                    });
                } else {
                        window.console.log("Response was not an array: " + data);
                }
            }
        });

    }

    doneTyping($event) {
        if ($event.which === 13) {
            this.addTodo($event.target.value);
            $event.target.value = null;
        }
    }
}
bootstrap(TodoList);